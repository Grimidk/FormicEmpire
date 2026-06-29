#!/usr/bin/env python3
"""Rename PNG assets under src/main/resources to PascalCase and update code references."""
from __future__ import annotations

import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
TEXT_SUFFIXES = {".java", ".sh", ".txt", ".md"}


def to_pascal_filename(name: str) -> str:
    base, ext = os.path.splitext(name)
    if not base:
        return name
    parts = re.split(r"[-_]", base)
    out = ""
    for part in parts:
        if not part:
            continue
        out += part[0].upper() + part[1:]
    return out + ext


def collect_pngs() -> list[Path]:
    return sorted(RES.rglob("*.png"))


def rename_pngs() -> dict[str, str]:
    mapping: dict[str, str] = {}
    paths = collect_pngs()
    planned: list[tuple[Path, Path, str, str]] = []
    for path in paths:
        new_name = to_pascal_filename(path.name)
        if new_name == path.name:
            continue
        new_path = path.with_name(new_name)
        rel_old = path.relative_to(RES).as_posix()
        rel_new = new_path.relative_to(RES).as_posix()
        planned.append((path, new_path, rel_old, rel_new))

    # Two-phase rename avoids case-insensitive filesystem collisions (macOS).
    temps: list[tuple[Path, Path, str, str]] = []
    for index, (path, new_path, rel_old, rel_new) in enumerate(planned):
        temp = path.with_name(f"__pascal_rename_{index}__.png")
        path.rename(temp)
        temps.append((temp, new_path, rel_old, rel_new))

    for temp, new_path, rel_old, rel_new in temps:
        temp.rename(new_path)
        mapping[rel_old] = rel_new
        print(f"renamed {rel_old} -> {rel_new}")
    return mapping


def update_text_files(mapping: dict[str, str]) -> None:
    if not mapping:
        return
    # Longest old paths first to avoid partial replacements.
    pairs = sorted(mapping.items(), key=lambda item: len(item[0]), reverse=True)
    for dirpath, _, filenames in os.walk(ROOT):
        if "target" in Path(dirpath).parts or ".git" in Path(dirpath).parts:
            continue
        for filename in filenames:
            path = Path(dirpath) / filename
            if path.suffix not in TEXT_SUFFIXES:
                continue
            if path.name == "rename_pngs_pascalcase.py":
                continue
            text = path.read_text(encoding="utf-8")
            original = text
            for old, new in pairs:
                text = text.replace(old, new)
            if text != original:
                path.write_text(text, encoding="utf-8")
                print(f"updated {path.relative_to(ROOT)}")


def main() -> None:
    mapping = rename_pngs()
    print(f"Renamed {len(mapping)} PNG(s)")
    update_text_files(mapping)


if __name__ == "__main__":
    main()
