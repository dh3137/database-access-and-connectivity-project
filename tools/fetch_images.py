#!/usr/bin/env python3
"""
fetch_images.py — Bulk-fetch Wikipedia images for cars with no imageUrl.
Usage: python3 tools/fetch_images.py
Requires: server running on http://localhost:8080
See workflows/fetch_images.md for full context.
"""

import json
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"

def get(path):
    with urllib.request.urlopen(BASE + path) as r:
        return json.loads(r.read())

cars = get("/api/cars")
missing = [c for c in cars if not c.get("imageUrl")]

if not missing:
    print("All cars already have an imageUrl.")
    exit(0)

print(f"Found {len(missing)} car(s) without an image:\n")

for car in missing:
    make  = urllib.parse.quote(car["make"])
    model = urllib.parse.quote(car["model"])
    year  = urllib.parse.quote(str(car["year"]))
    try:
        result = get(f"/api/carimage?make={make}&model={model}&year={year}")
        url = result.get("url", "— no image found")
    except Exception as e:
        url = f"— error: {e}"
    print(f"[{car['id']}] {car['year']} {car['make']} {car['model']} → {url}")
