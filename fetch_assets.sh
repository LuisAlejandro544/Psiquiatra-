#!/usr/bin/env bash
# ==============================================================================
# Sanatorio 3D - Scraping & Descarga Automatizada de Assets 3D y Texturas CC0
# ==============================================================================
# Busca y descarga modelos 3D y texturas de repositorios de Dominio Público (CC0)
# que no exigen código abierto, permiten monetización y no requieren créditos.
# Desempaqueta archivos sueltos (glTF, bin, OBJ, MTL y texturas en JPG/PNG)
# e inyecta armaduras de huesos (rigging) y animaciones editables.
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_BIN="$(which python3 || echo "")"

if [ -z "$PYTHON_BIN" ]; then
  echo "[-] Error: Se requiere python3 en el sistema." >&2
  exit 1
fi

# Pasar todos los argumentos al motor interno en Python
exec "$PYTHON_BIN" - "$@" << 'EOF'
import sys
import os
import json
import urllib.request
import struct
import math
import argparse
import shutil
import re

USER_AGENT = "SanatorioAssetScraper/1.0 (Android Indie Game Engine; CC0 Scraper)"
POLYHAVEN_API_MODELS = "https://api.polyhaven.com/assets?t=models"
POLYHAVEN_API_TEXTURES = "https://api.polyhaven.com/assets?t=textures"
POLYHAVEN_FILES_API = "https://api.polyhaven.com/files/"

# Diccionario semántico de traducción y expansión para términos de terror/psiquiátrico
SEMANTIC_MAPPINGS = {
    "silla": ["chair", "armchair", "seat", "stool"],
    "sillas": ["chair", "armchair", "seat", "stool"],
    "psiquiatra": ["hospital", "medical", "infirmary", "clinic", "wheelchair", "asylum", "psychiatric"],
    "psiquiatrico": ["hospital", "medical", "infirmary", "clinic", "wheelchair", "asylum", "psychiatric"],
    "manicomio": ["hospital", "asylum", "psychiatric", "medical", "clinic"],
    "sanatorio": ["hospital", "sanatorium", "asylum", "medical", "infirmary"],
    "rueda": ["wheel", "wheelchair"],
    "ruedas": ["wheel", "wheelchair"],
    "camilla": ["stretcher", "hospital_bed", "bed", "medical"],
    "cama": ["bed", "hospital_bed", "old_bed_frame", "cot"],
    "azulejo": ["tiles", "tile", "floor", "ceramic"],
    "azulejos": ["tiles", "tile", "floor", "ceramic"],
    "pared": ["wall", "plaster", "concrete", "grunge"],
    "piso": ["floor", "tiles", "concrete"],
    "suelo": ["floor", "tiles", "concrete"],
    "concreto": ["concrete", "cement"],
    "cemento": ["concrete", "cement"],
    "oxido": ["rust", "rusty", "metal", "worn"],
    "oxidado": ["rust", "rusty", "metal", "worn"],
    "sangre": ["blood", "grunge", "stain", "stained"],
    "metal": ["metal", "iron", "steel"],
    "madera": ["wood", "wooden"],
    "puerta": ["door", "gate"],
    "reja": ["gate", "bars", "prison", "grille"],
    "ventana": ["window", "glass"],
    "linterna": ["flashlight", "lamp", "lantern"],
    "botiquin": ["medical_box", "first_aid", "medical"],
    "vendaje": ["medical_tape", "bandage", "hospital"],
    "muleta": ["vintage_crutches_01", "crutches", "medical"],
    "muletas": ["vintage_crutches_01", "crutches", "medical"],
    "mano": ["garden_gloves_01", "glove", "hand", "arm"],
    "manos": ["garden_gloves_01", "glove", "hand", "arm"],
    "guante": ["garden_gloves_01", "glove", "hand"],
    "guantes": ["garden_gloves_01", "glove", "hand"],
    "brazo": ["garden_gloves_01", "arm", "hand"],
    "brazos": ["garden_gloves_01", "arm", "hand"]
}

def fetch_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))

def download_file(url, target_path):
    os.makedirs(os.path.dirname(target_path), exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=30) as resp, open(target_path, "wb") as f:
        shutil.copyfileobj(resp, f)

def score_asset(asset_id, asset_data, keywords):
    score = 0
    name = asset_data.get("name", "").lower()
    tags = [t.lower() for t in asset_data.get("tags", []) + asset_data.get("categories", [])]
    asset_id_low = asset_id.lower()

    for kw in keywords:
        kw = kw.lower()
        if kw == asset_id_low:
            score += 50
        elif kw in asset_id_low:
            score += 25
        if kw in name:
            score += 20
        for t in tags:
            if kw == t:
                score += 15
            elif kw in t:
                score += 8
    return score

def expand_query_keywords(query_text):
    words = re.findall(r'\b\w+\b', query_text.lower())
    keywords = set(words)
    for word in words:
        if word in SEMANTIC_MAPPINGS:
            for kw in SEMANTIC_MAPPINGS[word]:
                keywords.add(kw)
    return list(keywords)

def extract_obj_and_mtl_from_gltf(gltf_path, bin_path, output_obj_path, output_mtl_path, texture_filename):
    with open(gltf_path, "r", encoding="utf-8") as f:
        gltf = json.load(f)
    with open(bin_path, "rb") as f:
        bin_data = f.read()

    if not gltf.get("meshes") or not gltf["meshes"][0].get("primitives"):
        return False

    prim = gltf["meshes"][0]["primitives"][0]
    attrs = prim.get("attributes", {})

    def read_buffer(acc_idx):
        acc = gltf["accessors"][acc_idx]
        bv = gltf["bufferViews"][acc["bufferView"]]
        offset = bv.get("byteOffset", 0) + acc.get("byteOffset", 0)
        length = bv["byteLength"]
        return bin_data[offset:offset+length], acc

    if "POSITION" not in attrs or "indices" not in prim:
        return False

    pos_bytes, pos_acc = read_buffer(attrs["POSITION"])
    norm_bytes, norm_acc = read_buffer(attrs["NORMAL"]) if "NORMAL" in attrs else (None, None)
    uv_bytes, uv_acc = read_buffer(attrs["TEXCOORD_0"]) if "TEXCOORD_0" in attrs else (None, None)
    ind_bytes, ind_acc = read_buffer(prim["indices"])

    mtl_filename = os.path.basename(output_mtl_path)
    mat_name = "Material_Sanatorio_CC0"

    # Escribir archivo MTL
    with open(output_mtl_path, "w", encoding="utf-8") as fmtl:
        fmtl.write(f"# Material generado por SanatorioAssetScraper (Licencia CC0)\n")
        fmtl.write(f"newmtl {mat_name}\n")
        fmtl.write("Ka 0.2 0.2 0.2\n")
        fmtl.write("Kd 0.8 0.8 0.8\n")
        fmtl.write("Ks 0.1 0.1 0.1\n")
        fmtl.write("d 1.0\n")
        fmtl.write("illum 2\n")
        if texture_filename:
            fmtl.write(f"map_Kd {texture_filename}\n")

    # Escribir archivo OBJ
    with open(output_obj_path, "w", encoding="utf-8") as fobj:
        fobj.write(f"# Modelo 3D exportado por SanatorioAssetScraper (Licencia CC0)\n")
        fobj.write(f"mtllib {mtl_filename}\n")
        fobj.write(f"o {os.path.splitext(os.path.basename(output_obj_path))[0]}\n")

        v_count = pos_acc["count"]
        for i in range(v_count):
            x, y, z = struct.unpack_from("<fff", pos_bytes, i * 12)
            fobj.write(f"v {x:.4f} {y:.4f} {z:.4f}\n")

        if norm_acc:
            for i in range(norm_acc["count"]):
                nx, ny, nz = struct.unpack_from("<fff", norm_bytes, i * 12)
                fobj.write(f"vn {nx:.4f} {ny:.4f} {nz:.4f}\n")

        if uv_acc:
            for i in range(uv_acc["count"]):
                u, v = struct.unpack_from("<ff", uv_bytes, i * 8)
                fobj.write(f"vt {u:.4f} {1.0 - v:.4f}\n")

        fobj.write(f"usemtl {mat_name}\ns 1\n")

        ind_count = ind_acc["count"]
        fmt = "<III" if ind_acc["componentType"] == 5125 else "<HHH"
        elem_size = 12 if ind_acc["componentType"] == 5125 else 6

        has_n = norm_acc is not None
        has_uv = uv_acc is not None

        for i in range(0, ind_count, 3):
            i1, i2, i3 = struct.unpack_from(fmt, ind_bytes, i * (elem_size // 3))
            p1, p2, p3 = i1 + 1, i2 + 1, i3 + 1
            if has_uv and has_n:
                fobj.write(f"f {p1}/{p1}/{p1} {p2}/{p2}/{p2} {p3}/{p3}/{p3}\n")
            elif has_uv:
                fobj.write(f"f {p1}/{p1} {p2}/{p2} {p3}/{p3}\n")
            elif has_n:
                fobj.write(f"f {p1}//{p1} {p2}//{p2} {p3}//{p3}\n")
            else:
                fobj.write(f"f {p1} {p2} {p3}\n")
    return True

def inject_bones_and_rig(gltf_path, bin_path, asset_type_name="chair"):
    with open(gltf_path, "r", encoding="utf-8") as f:
        gltf = json.load(f)
    with open(bin_path, "rb") as f:
        bin_data = bytearray(f.read())

    is_hand = any(w in asset_type_name.lower() for w in ["hand", "glove", "arm", "mano", "guante", "brazo"])

    if is_hand:
        # Huesos anatómicos de mano / guante de investigación forense
        bones_hierarchy = [
            {"name": "Bone_Forearm_Wrist", "translation": [0.0, -0.15, 0.0], "children": [2]},
            {"name": "Bone_Hand_Palm", "translation": [0.0, 0.05, 0.0], "children": [3, 5, 7, 8]},
            {"name": "Bone_Thumb_Metacarpal", "translation": [-0.04, 0.02, 0.02], "children": [4]},
            {"name": "Bone_Thumb_Phalanx", "translation": [-0.02, 0.04, 0.02], "children": []},
            {"name": "Bone_Index_Proximal", "translation": [-0.02, 0.08, 0.0], "children": [6]},
            {"name": "Bone_Index_Distal", "translation": [0.0, 0.05, 0.0], "children": []},
            {"name": "Bone_Middle_Finger", "translation": [0.01, 0.09, 0.0], "children": []},
            {"name": "Bone_Ring_Pinky_Group", "translation": [0.03, 0.07, 0.0], "children": []}
        ]
        rig_name = "Armature_Investigator_Hand"
        anim_1_name = "Anim_Flashlight_Grip"
        anim_2_name = "Anim_Tremble_Insanity"
    else:
        # Huesos modulares para utilería médica y sillas psiquiátricas
        bones_hierarchy = [
            {"name": "Armature_Root", "translation": [0.0, 0.0, 0.0], "children": [2, 4, 5, 6, 7]},
            {"name": "Bone_Chassis_Base", "translation": [0.0, 0.40, 0.0], "children": [3]},
            {"name": "Bone_Backrest_Tilt", "translation": [0.0, 0.35, -0.25], "children": []},
            {"name": "Bone_Wheel_Left", "translation": [-0.38, 0.32, 0.0], "children": []},
            {"name": "Bone_Wheel_Right", "translation": [0.38, 0.32, 0.0], "children": []},
            {"name": "Bone_Restraint_L", "translation": [-0.35, 0.48, 0.10], "children": []},
            {"name": "Bone_Restraint_R", "translation": [0.35, 0.48, 0.10], "children": []}
        ]
        rig_name = "Armature_Sanatorio_Rig"
        anim_1_name = "Anim_Wheel_Roll_Loop"
        anim_2_name = "Anim_Backrest_Creak_And_Tilt"

    total_bones = len(bones_hierarchy)
    
    # 1. Empaquetar matrices de enlace inverso (Inverse Bind Matrices - 4x4 identidad)
    mat_offset = len(bin_data)
    for _ in range(total_bones):
        bin_data.extend(struct.pack("<16f", 1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1))
    mat_len = total_bones * 64

    bv_mat_idx = len(gltf["bufferViews"])
    gltf["bufferViews"].append({
        "buffer": 0,
        "byteOffset": mat_offset,
        "byteLength": mat_len
    })

    acc_mat_idx = len(gltf["accessors"])
    gltf["accessors"].append({
        "bufferView": bv_mat_idx,
        "componentType": 5126,
        "count": total_bones,
        "type": "MAT4"
    })

    # 2. Empaquetar animación: Rotación continua de ruedas (Anim_Asylum_Wheel_Roll)
    time_points = [0.0, 0.5, 1.0, 1.5, 2.0]
    time_offset = len(bin_data)
    for t in time_points:
        bin_data.extend(struct.pack("<f", t))
    time_len = len(time_points) * 4

    bv_time_idx = len(gltf["bufferViews"])
    gltf["bufferViews"].append({
        "buffer": 0,
        "byteOffset": time_offset,
        "byteLength": time_len
    })

    acc_time_idx = len(gltf["accessors"])
    gltf["accessors"].append({
        "bufferView": bv_time_idx,
        "componentType": 5126,
        "count": len(time_points),
        "type": "SCALAR",
        "min": [0.0],
        "max": [2.0]
    })

    # Rotaciones cuaternión sobre eje X
    rot_offset = len(bin_data)
    angles = [0.0, math.pi/2, math.pi, 3*math.pi/2, 2*math.pi]
    for a in angles:
        s = math.sin(a / 2)
        c = math.cos(a / 2)
        bin_data.extend(struct.pack("<4f", s, 0.0, 0.0, c))
    rot_len = len(angles) * 16

    bv_rot_idx = len(gltf["bufferViews"])
    gltf["bufferViews"].append({
        "buffer": 0,
        "byteOffset": rot_offset,
        "byteLength": rot_len
    })

    acc_rot_idx = len(gltf["accessors"])
    gltf["accessors"].append({
        "bufferView": bv_rot_idx,
        "componentType": 5126,
        "count": len(angles),
        "type": "VEC4"
    })

    # 3. Animación de crujido y reclinación del respaldo (Anim_Creak_Tilt)
    creak_times = [0.0, 1.0, 2.0, 3.0, 4.0]
    creak_time_offset = len(bin_data)
    for t in creak_times:
        bin_data.extend(struct.pack("<f", t))
    creak_time_len = len(creak_times) * 4

    bv_creak_time_idx = len(gltf["bufferViews"])
    gltf["bufferViews"].append({
        "buffer": 0,
        "byteOffset": creak_time_offset,
        "byteLength": creak_time_len
    })

    acc_creak_time_idx = len(gltf["accessors"])
    gltf["accessors"].append({
        "bufferView": bv_creak_time_idx,
        "componentType": 5126,
        "count": len(creak_times),
        "type": "SCALAR",
        "min": [0.0],
        "max": [4.0]
    })

    # Cuaterniones con ligeras inclinaciones angulares (-3 a +3 grados)
    creak_rot_offset = len(bin_data)
    creak_angles = [0.0, -0.06, 0.0, 0.06, 0.0]
    for ca in creak_angles:
        s = math.sin(ca / 2)
        c = math.cos(ca / 2)
        bin_data.extend(struct.pack("<4f", s, 0.0, 0.0, c))
    creak_rot_len = len(creak_angles) * 16

    bv_creak_rot_idx = len(gltf["bufferViews"])
    gltf["bufferViews"].append({
        "buffer": 0,
        "byteOffset": creak_rot_offset,
        "byteLength": creak_rot_len
    })

    acc_creak_rot_idx = len(gltf["accessors"])
    gltf["accessors"].append({
        "bufferView": bv_creak_rot_idx,
        "componentType": 5126,
        "count": len(creak_angles),
        "type": "VEC4"
    })

    # Actualizar longitud total del buffer binario
    gltf["buffers"][0]["byteLength"] = len(bin_data)

    # Inyectar nodos de huesos en el árbol glTF
    mesh_node_idx = 0
    if gltf.get("nodes"):
        gltf["nodes"][mesh_node_idx]["skin"] = 0
    
    root_bone_idx = len(gltf["nodes"])
    joint_indices = []

    for i, bone in enumerate(bones_hierarchy):
        bone_node_idx = root_bone_idx + i
        joint_indices.append(bone_node_idx)
        node_obj = {
            "name": bone["name"],
            "translation": bone["translation"]
        }
        if bone.get("children"):
            node_obj["children"] = [root_bone_idx + c - 1 for c in bone["children"]]
        gltf["nodes"].append(node_obj)

    # Conectar en la escena principal
    if gltf.get("scenes") and len(gltf["scenes"]) > 0:
        scene_nodes = gltf["scenes"][0].get("nodes", [0])
        if root_bone_idx not in scene_nodes:
            scene_nodes.append(root_bone_idx)
        gltf["scenes"][0]["nodes"] = scene_nodes

    # Inyectar skin
    gltf["skins"] = [{
        "name": rig_name,
        "skeleton": root_bone_idx,
        "joints": joint_indices,
        "inverseBindMatrices": acc_mat_idx
    }]

    # Inyectar animaciones
    if is_hand:
        # Animaciones para manos de investigador
        # Anim 1: Agarre de la linterna (flexión de falanges)
        # Anim 2: Temblor de cordura (sacudida rápida de muñeca)
        gltf["animations"] = [
            {
                "name": anim_1_name,
                "samplers": [{
                    "input": acc_time_idx,
                    "interpolation": "LINEAR",
                    "output": acc_rot_idx
                }],
                "channels": [
                    {"sampler": 0, "target": {"node": root_bone_idx + 2, "path": "rotation"}}, # Pulgar
                    {"sampler": 0, "target": {"node": root_bone_idx + 4, "path": "rotation"}}, # Índice
                    {"sampler": 0, "target": {"node": root_bone_idx + 6, "path": "rotation"}}  # Medio
                ]
            },
            {
                "name": anim_2_name,
                "samplers": [{
                    "input": acc_creak_time_idx,
                    "interpolation": "LINEAR",
                    "output": acc_creak_rot_idx
                }],
                "channels": [
                    {"sampler": 0, "target": {"node": root_bone_idx, "path": "rotation"}},     # Muñeca
                    {"sampler": 0, "target": {"node": root_bone_idx + 1, "path": "rotation"}}  # Palma
                ]
            }
        ]
        anim_1_target = "Dedos índice, pulgar y medio (agarre de linterna)"
        anim_2_target = "Muñeca y palma (temblor por baja cordura)"
    else:
        wheel_left_idx = root_bone_idx + 3
        wheel_right_idx = root_bone_idx + 4
        backrest_idx = root_bone_idx + 2
        gltf["animations"] = [
            {
                "name": anim_1_name,
                "samplers": [{
                    "input": acc_time_idx,
                    "interpolation": "LINEAR",
                    "output": acc_rot_idx
                }],
                "channels": [
                    {"sampler": 0, "target": {"node": wheel_left_idx, "path": "rotation"}},
                    {"sampler": 0, "target": {"node": wheel_right_idx, "path": "rotation"}}
                ]
            },
            {
                "name": anim_2_name,
                "samplers": [{
                    "input": acc_creak_time_idx,
                    "interpolation": "LINEAR",
                    "output": acc_creak_rot_idx
                }],
                "channels": [
                    {"sampler": 0, "target": {"node": backrest_idx, "path": "rotation"}}
                ]
            }
        ]
        anim_1_target = "Ruedas izquierda y derecha"
        anim_2_target = "Respaldo articulado"

    # Guardar archivos finales
    with open(gltf_path, "w", encoding="utf-8") as f:
        json.dump(gltf, f, indent=2)

    with open(bin_path, "wb") as f:
        f.write(bin_data)

    # Guardar esquema de documentación de huesos
    rig_doc = {
        "armature_name": rig_name,
        "total_joints": total_bones,
        "bones": [
            {
                "id": root_bone_idx + i,
                "name": b["name"],
                "default_translation": b["translation"],
                "role": "Hueso posicional / rotacional modificable en tiempo de ejecución"
            } for i, b in enumerate(bones_hierarchy)
        ],
        "animations": [
            {"name": anim_1_name, "target": anim_1_target, "duration_sec": 2.0},
            {"name": anim_2_name, "target": anim_2_target, "duration_sec": 4.0}
        ],
        "instructions": "Puedes rotar o trasladar cualquiera de estos huesos mediante código o herramientas 3D."
    }
    return rig_doc

def main():
    parser = argparse.ArgumentParser(
        description="Scraper y descargador de assets 3D y texturas CC0 para Sanatorio 3D."
    )
    parser.add_argument("query", help="Descripción o palabras clave del asset (ej: 'sillas de centro psiquiatra')")
    parser.add_argument("-t", "--type", choices=["auto", "model", "texture"], default="auto",
                        help="Tipo de asset a buscar (auto detecta modelos vs texturas)")
    parser.add_argument("-r", "--resolution", choices=["1k", "2k", "4k"], default="1k",
                        help="Resolución de texturas y mallas (1k recomendada para móvil)")
    parser.add_argument("-o", "--output", default="",
                        help="Directorio de salida personalizado")
    parser.add_argument("-l", "--list", action="store_true",
                        help="Listar mejores coincidencias sin descargar")
    parser.add_argument("--no-rig", action="store_true",
                        help="No inyectar huesos ni animaciones al modelo 3D")

    args = parser.parse_args()

    # Detección automática de modelo vs textura si es "auto"
    target_type = args.type
    texture_clues = ["textura", "azulejo", "azulejos", "baldosa", "baldosas", "pared", "muro", "piso", "suelo", "yeso"]
    if target_type == "auto":
        is_texture = any(clue in args.query.lower() for clue in texture_clues)
        target_type = "texture" if is_texture else "model"

    print(f"[*] Buscando '{args.query}' (Categoría: {target_type.upper()}) en repositorios CC0...")
    keywords = expand_query_keywords(args.query)
    print(f"[*] Palabras clave expandidas: {', '.join(keywords)}")

    api_url = POLYHAVEN_API_MODELS if target_type == "model" else POLYHAVEN_API_TEXTURES
    catalog = fetch_json(api_url)

    # Evaluar y puntuar candidatos
    candidates = []
    for asset_id, asset_data in catalog.items():
        score = score_asset(asset_id, asset_data, keywords)
        if score > 0:
            candidates.append((score, asset_id, asset_data))

    candidates.sort(key=lambda x: x[0], reverse=True)

    if not candidates:
        print(f"[-] No se encontraron coincidencias exactas para '{args.query}'.")
        sys.exit(1)

    print(f"\n[+] Coincidencias encontradas ({len(candidates)}):")
    for score, aid, data in candidates[:5]:
        tags_str = ", ".join(data.get("tags", [])[:5])
        print(f"    - [{score} pts] {aid} ('{data.get('name')}') -> Etiquetas: {tags_str}")

    if args.list:
        sys.exit(0)

    best_score, best_id, best_data = candidates[0]
    print(f"\n[+] Seleccionado mejor asset: '{best_id}' ({best_data.get('name')})")

    # Obtener catálogo de archivos descargables
    files_url = f"{POLYHAVEN_FILES_API}{best_id}"
    files_catalog = fetch_json(files_url)

    # Determinar directorio de destino
    script_root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..")) if os.path.dirname(__file__) else os.getcwd()
    if args.output:
        dest_dir = os.path.abspath(args.output)
    else:
        base_cat = "models" if target_type == "model" else "textures"
        dest_dir = os.path.join(script_root, "app", "src", "main", "assets", base_cat, best_id.lower())

    os.makedirs(dest_dir, exist_ok=True)
    res = args.resolution
    print(f"[*] Descargando a: {dest_dir} (Resolución: {res})...")

    downloaded_files = []
    primary_diffuse_file = ""

    if target_type == "model":
        gltf_section = files_catalog.get("gltf", {}).get(res, {}).get("gltf", {})
        if not gltf_section:
            # Intentar fallback a otra resolución si la pedida no existe
            for alt_res in ["1k", "2k", "4k"]:
                if alt_res in files_catalog.get("gltf", {}):
                    gltf_section = files_catalog["gltf"][alt_res]["gltf"]
                    res = alt_res
                    break

        if not gltf_section:
            print("[-] No se encontró versión glTF en el catálogo del asset.")
            sys.exit(1)

        # 1. Descargar archivo glTF
        gltf_filename = f"{best_id.lower()}.gltf"
        gltf_path = os.path.join(dest_dir, gltf_filename)
        print(f"    -> Descargando {gltf_filename}...")
        download_file(gltf_section["url"], gltf_path)
        downloaded_files.append(gltf_filename)

        # 2. Descargar archivos incluidos (.bin y texturas en JPG/PNG)
        bin_path = ""
        includes = gltf_section.get("include", {})
        for inc_rel_path, inc_meta in includes.items():
            print(f"    -> Descargando {inc_rel_path}...")
            inc_target = os.path.join(dest_dir, inc_rel_path)
            download_file(inc_meta["url"], inc_target)
            downloaded_files.append(inc_rel_path)
            if inc_rel_path.endswith(".bin"):
                bin_path = inc_target
            elif "diff" in inc_rel_path or "col" in inc_rel_path:
                primary_diffuse_file = inc_rel_path

        # 3. Extraer formato OBJ y MTL para tener archivos sueltos completos
        obj_filename = f"{best_id.lower()}.obj"
        mtl_filename = f"{best_id.lower()}.mtl"
        obj_path = os.path.join(dest_dir, obj_filename)
        mtl_path = os.path.join(dest_dir, mtl_filename)

        if bin_path and os.path.exists(bin_path):
            print(f"[*] Generando malla suelta OBJ y materiales MTL...")
            if extract_obj_and_mtl_from_gltf(gltf_path, bin_path, obj_path, mtl_path, primary_diffuse_file):
                downloaded_files.extend([obj_filename, mtl_filename])

        # 4. Inyección de Huesos y Armadura (Rigging & Animación)
        if not args.no_rig and bin_path and os.path.exists(bin_path):
            print(f"[*] Inyectando sistema de huesos y animaciones en {gltf_filename}...")
            rig_doc = inject_bones_and_rig(gltf_path, bin_path, asset_type_name=best_id.lower())
            rig_info_path = os.path.join(dest_dir, "rig_info.json")
            with open(rig_info_path, "w", encoding="utf-8") as frig:
                json.dump(rig_doc, frig, indent=2)
            downloaded_files.append("rig_info.json")
            anim_names = ", ".join([f"'{a['name']}'" for a in rig_doc.get("animations", [])])
            print(f"    [✓] Armadura configurada con {rig_doc['total_joints']} huesos modificables.")
            print(f"    [✓] Animaciones inyectadas: {anim_names}.")

    else:
        # Descarga de texturas PBR sueltas
        for map_type in ["Diffuse", "Normal", "Roughness", "ARM", "Displacement"]:
            if map_type in files_catalog:
                map_res = files_catalog[map_type].get(res, {})
                format_choice = "jpg" if "jpg" in map_res else ("png" if "png" in map_res else None)
                if format_choice:
                    file_info = map_res[format_choice]
                    filename = f"{best_id.lower()}_{map_type.lower()}.{format_choice}"
                    target_file = os.path.join(dest_dir, filename)
                    print(f"    -> Descargando mapa {map_type} ({filename})...")
                    download_file(file_info["url"], target_file)
                    downloaded_files.append(filename)

    # 5. Generar archivo de licencia CC0 garantizada
    license_path = os.path.join(dest_dir, "LICENSE_CC0.txt")
    with open(license_path, "w", encoding="utf-8") as flic:
        flic.write(f"Asset: {best_data.get('name')} ({best_id})\n")
        flic.write("Origen: Poly Haven (Repositorio de Dominio Público)\n")
        flic.write("Licencia: Creative Commons Zero (CC0 1.0 Universal - Public Domain Dedication)\n\n")
        flic.write("Términos Legales:\n")
        flic.write("- Uso comercial libre permitido (sin pagos de regalías ni licencias).\n")
        flic.write("- No obliga a que el proyecto o aplicación sea de código abierto.\n")
        flic.write("- No requiere dar atribución obligatoria ni créditos forzosos.\n")
        flic.write("- Se permite modificar, redistribuir y empaquetar libremente en el APK.\n")
    downloaded_files.append("LICENSE_CC0.txt")

    print("\n" + "=" * 60)
    print(f"[✓] Asset '{best_id}' descargado y desempaquetado con éxito:")
    for f in downloaded_files:
        print(f"    • {f}")
    print(f"Ubicación: {dest_dir}")
    print("=" * 60 + "\n")

if __name__ == "__main__":
    main()
EOF
