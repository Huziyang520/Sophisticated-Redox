#!/usr/bin/env python3
"""Adds the new settings-button tooltip keys to all 13 language files.

The 13 files must keep exactly the same key set as en_us (that is a hard rule of this project),
so the keys are appended everywhere in one pass. Files are written as UTF-8 without BOM, which
is also a hard rule, and never with \\u escapes because the language files go through Gradle's
expand step.

Usage:  python tools/gen_lang.py
"""

import json
import os
import collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "sophisticated_redox", "lang")

LANGS = ["en_us", "en_gb", "zh_cn", "zh_tw", "zh_hk", "lzh", "ru_ru", "de_de", "ja_jp", "fr_fr", "es_es", "pt_br", "ko_kr"]

B = "gui.sophisticated_redox.button."

TEXT = {
    "en_us": {
        B + "level.1": "Level Ⅰ - one conversion step",
        B + "level.2": "Level Ⅱ - two conversion steps",
        B + "level.3": "Level Ⅲ - three conversion steps",
        B + "level.4": "Level Ⅳ - strip the wax, then three reduction steps",
        B + "dewax.on": "Dewaxing allowed - waxed blocks are unwaxed first, then reduced",
        B + "dewax.off": "Dewaxing blocked - waxed blocks are left alone",
        B + "shape_match.on": "Shape match on - the filter matches every oxidation stage and waxed variant of the same shape",
        B + "shape_match.off": "Shape match off - the filter matches exact items",
    },
    "en_gb": {
        B + "level.1": "Level Ⅰ - one conversion step",
        B + "level.2": "Level Ⅱ - two conversion steps",
        B + "level.3": "Level Ⅲ - three conversion steps",
        B + "level.4": "Level Ⅳ - strip the wax, then three reduction steps",
        B + "dewax.on": "Dewaxing allowed - waxed blocks are unwaxed first, then reduced",
        B + "dewax.off": "Dewaxing blocked - waxed blocks are left alone",
        B + "shape_match.on": "Shape match on - the filter matches every oxidation stage and waxed variant of the same shape",
        B + "shape_match.off": "Shape match off - the filter matches exact items",
    },
    "zh_cn": {
        B + "level.1": "档位 Ⅰ：转换一次",
        B + "level.2": "档位 Ⅱ：转换两次",
        B + "level.3": "档位 Ⅲ：转换三次",
        B + "level.4": "档位 Ⅳ：先脱蜡，再除锈三次",
        B + "dewax.on": "允许脱蜡：先去掉涂蜡，再进行还原",
        B + "dewax.off": "不允许脱蜡：跳过已涂蜡的方块",
        B + "shape_match.on": "匹配方块形状：过滤器命中同一形状的所有氧化档位与涂蜡变体",
        B + "shape_match.off": "精确匹配：过滤器只命中相同的物品",
    },
    "zh_tw": {
        B + "level.1": "檔位 Ⅰ：轉換一次",
        B + "level.2": "檔位 Ⅱ：轉換兩次",
        B + "level.3": "檔位 Ⅲ：轉換三次",
        B + "level.4": "檔位 Ⅳ：先脫蠟，再除鏽三次",
        B + "dewax.on": "允許脫蠟：先去掉塗蠟，再進行還原",
        B + "dewax.off": "不允許脫蠟：跳過已塗蠟的方塊",
        B + "shape_match.on": "匹配方塊形狀：過濾器命中同一形狀的所有氧化檔位與塗蠟變體",
        B + "shape_match.off": "精確匹配：過濾器只命中相同的物品",
    },
    "zh_hk": {
        B + "level.1": "檔位 Ⅰ：轉換一次",
        B + "level.2": "檔位 Ⅱ：轉換兩次",
        B + "level.3": "檔位 Ⅲ：轉換三次",
        B + "level.4": "檔位 Ⅳ：先脫蠟，再除鏽三次",
        B + "dewax.on": "允許脫蠟：先去除塗蠟，再進行還原",
        B + "dewax.off": "不允許脫蠟：略過已塗蠟的方塊",
        B + "shape_match.on": "匹配方塊形狀：過濾器命中同一形狀的所有氧化檔位與塗蠟變體",
        B + "shape_match.off": "精確匹配：過濾器只命中相同的物品",
    },
    "lzh": {
        B + "level.1": "其第一：一化",
        B + "level.2": "其第二：二化",
        B + "level.3": "其第三：三化",
        B + "level.4": "其第四：先去蠟，後三除鏽",
        B + "dewax.on": "許脫蠟：先去其蠟，後乃還原",
        B + "dewax.off": "不許脫蠟：蠟者不犯",
        B + "shape_match.on": "同形相配：凡同形之銅，雖鏽蝕塗蠟，皆為所中",
        B + "shape_match.off": "正配：惟同物乃中",
    },
    "ru_ru": {
        B + "level.1": "Уровень Ⅰ — одно преобразование",
        B + "level.2": "Уровень Ⅱ — два преобразования",
        B + "level.3": "Уровень Ⅲ — три преобразования",
        B + "level.4": "Уровень Ⅳ — снять воск, затем три восстановления",
        B + "dewax.on": "Снятие воска разрешено — залитый воском блок сначала очищается, затем восстанавливается",
        B + "dewax.off": "Снятие воска запрещено — залитые воском блоки пропускаются",
        B + "shape_match.on": "Совпадение по форме — фильтр находит все стадии окисления и восковые варианты той же формы",
        B + "shape_match.off": "Точное совпадение — фильтр находит только те же предметы",
    },
    "de_de": {
        B + "level.1": "Stufe Ⅰ - ein Umwandlungsschritt",
        B + "level.2": "Stufe Ⅱ - zwei Umwandlungsschritte",
        B + "level.3": "Stufe Ⅲ - drei Umwandlungsschritte",
        B + "level.4": "Stufe Ⅳ - Wachs entfernen, dann drei Reduktionsschritte",
        B + "dewax.on": "Entwachsen erlaubt - gewachste Blöcke werden erst entwachst, dann reduziert",
        B + "dewax.off": "Entwachsen gesperrt - gewachste Blöcke werden übersprungen",
        B + "shape_match.on": "Formabgleich an - der Filter trifft jede Oxidationsstufe und Wachsvariante derselben Form",
        B + "shape_match.off": "Formabgleich aus - der Filter trifft exakte Gegenstände",
    },
    "ja_jp": {
        B + "level.1": "段階 Ⅰ：変換1回",
        B + "level.2": "段階 Ⅱ：変換2回",
        B + "level.3": "段階 Ⅲ：変換3回",
        B + "level.4": "段階 Ⅳ：ワックスを除去してから3回還元",
        B + "dewax.on": "脱蠟あり：ワックスがけされたブロックも処理する",
        B + "dewax.off": "脱蠟なし：ワックスがけされたブロックは対象外",
        B + "shape_match.on": "形状一致：同じ形状の酸化段階とワックス変種をすべて対象にする",
        B + "shape_match.off": "完全一致：同じアイテムのみ対象",
    },
    "fr_fr": {
        B + "level.1": "Niveau Ⅰ - une étape de conversion",
        B + "level.2": "Niveau Ⅱ - deux étapes de conversion",
        B + "level.3": "Niveau Ⅲ - trois étapes de conversion",
        B + "level.4": "Niveau Ⅳ - retire la cire, puis trois étapes de réduction",
        B + "dewax.on": "Décirage autorisé - les blocs cirés sont d'abord décirés puis réduits",
        B + "dewax.off": "Décirage bloqué - les blocs cirés sont ignorés",
        B + "shape_match.on": "Correspondance par forme activée - le filtre concerne tous les stades d'oxydation et variantes cirées de la même forme",
        B + "shape_match.off": "Correspondance par forme désactivée - le filtre ne concerne que les objets identiques",
    },
    "es_es": {
        B + "level.1": "Nivel Ⅰ - un paso de conversión",
        B + "level.2": "Nivel Ⅱ - dos pasos de conversión",
        B + "level.3": "Nivel Ⅲ - tres pasos de conversión",
        B + "level.4": "Nivel Ⅳ - retira la cera y luego tres pasos de reducción",
        B + "dewax.on": "Desencerado permitido - los bloques encerados se desenceran primero y luego se reducen",
        B + "dewax.off": "Desencerado bloqueado - los bloques encerados se omiten",
        B + "shape_match.on": "Coincidencia por forma activada - el filtro abarca todos los estados de oxidación y variantes enceradas de la misma forma",
        B + "shape_match.off": "Coincidencia por forma desactivada - el filtro solo abarca objetos idénticos",
    },
    "pt_br": {
        B + "level.1": "Nível Ⅰ - um passo de conversão",
        B + "level.2": "Nível Ⅱ - dois passos de conversão",
        B + "level.3": "Nível Ⅲ - três passos de conversão",
        B + "level.4": "Nível Ⅳ - remove a cera e depois três passos de redução",
        B + "dewax.on": "Desenceração permitida - blocos encerados são desencerados antes de serem reduzidos",
        B + "dewax.off": "Desenceração bloqueada - blocos encerados são ignorados",
        B + "shape_match.on": "Correspondência por forma ativada - o filtro alcança todos os estágios de oxidação e variantes enceradas da mesma forma",
        B + "shape_match.off": "Correspondência por forma desativada - o filtro alcança apenas itens idênticos",
    },
    "ko_kr": {
        B + "level.1": "단계 Ⅰ: 변환 1회",
        B + "level.2": "단계 Ⅱ: 변환 2회",
        B + "level.3": "단계 Ⅲ: 변환 3회",
        B + "level.4": "단계 Ⅳ: 왁스 제거 후 3회 환원",
        B + "dewax.on": "왁스 제거 허용: 왁스칠된 블록을 먼저 벗긴 뒤 환원합니다",
        B + "dewax.off": "왁스 제거 금지: 왁스칠된 블록은 건너뜁니다",
        B + "shape_match.on": "모양 일치: 같은 모양의 모든 산화 단계와 왁스 변형을 대상으로 합니다",
        B + "shape_match.off": "정확한 일치: 동일한 아이템만 대상으로 합니다",
    },
}

NEW_KEYS = list(TEXT["en_us"].keys())

# Second pass: wording that must make explicit that dewaxing counts as ONE step - the earlier
# phrasing read as if a single insertion both dewaxed and reduced, which is not how it works.
V2 = {
    "en_us": {
        B + "level.4": "Level Ⅳ - four conversion steps",
        B + "dewax.on": "Dewaxing allowed - dewaxing counts as one step, the remaining steps reduce",
    },
    "en_gb": {
        B + "level.4": "Level Ⅳ - four conversion steps",
        B + "dewax.on": "Dewaxing allowed - dewaxing counts as one step, the remaining steps reduce",
    },
    "zh_cn": {
        B + "level.4": "档位 Ⅳ：转换四次",
        B + "dewax.on": "允许脱蜡：脱蜡本身算一步，其余步骤才除锈",
    },
    "zh_tw": {
        B + "level.4": "檔位 Ⅳ：轉換四次",
        B + "dewax.on": "允許脫蠟：脫蠟本身算一步，其餘步驟才除鏽",
    },
    "zh_hk": {
        B + "level.4": "檔位 Ⅳ：轉換四次",
        B + "dewax.on": "允許脫蠟：脫蠟本身算一步，其餘步驟才除鏽",
    },
    "lzh": {
        B + "level.4": "其第四：凡四化",
        B + "dewax.on": "許脫蠟：去蠟計一步，餘步乃除鏽",
    },
    "ru_ru": {
        B + "level.4": "Уровень Ⅳ — четыре преобразования",
        B + "dewax.on": "Снятие воска разрешено — снятие воска считается отдельным шагом, остальные шаги восстанавливают",
    },
    "de_de": {
        B + "level.4": "Stufe Ⅳ - vier Umwandlungsschritte",
        B + "dewax.on": "Entwachsen erlaubt - Entwachsen zählt als eigener Schritt, die übrigen Schritte reduzieren",
    },
    "ja_jp": {
        B + "level.4": "段階 Ⅳ：変換4回",
        B + "dewax.on": "脱蠟あり：脱蠟自体が1ステップとして数えられ、残りのステップで還元します",
    },
    "fr_fr": {
        B + "level.4": "Niveau Ⅳ - quatre étapes de conversion",
        B + "dewax.on": "Décirage autorisé - le décirage compte comme une étape, les étapes restantes réduisent",
    },
    "es_es": {
        B + "level.4": "Nivel Ⅳ - cuatro pasos de conversión",
        B + "dewax.on": "Desencerado permitido - el desencerado cuenta como un paso; los demás pasos reducen",
    },
    "pt_br": {
        B + "level.4": "Nível Ⅳ - quatro passos de conversão",
        B + "dewax.on": "Desenceração permitida - desencerar conta como um passo; os demais passos reduzem",
    },
    "ko_kr": {
        B + "level.4": "단계 Ⅳ: 변환 4회",
        B + "dewax.on": "왁스 제거 허용: 왁스 제거 자체가 1단계로 계산되며 나머지 단계는 환원합니다",
    },
}


def main():
    for lang in LANGS:
        path = os.path.join(LANG_DIR, lang + ".json")
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f, object_pairs_hook=collections.OrderedDict)
        for key in NEW_KEYS:
            data[key] = TEXT[lang][key]
        for key, value in V2.get(lang, {}).items():
            data[key] = value
        text = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
        assert "\\u" not in text, lang + ": escaped unicode would break Gradle expand"
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(text)
        print(lang, len(data), "keys")
    print("languages updated")


if __name__ == "__main__":
    main()
