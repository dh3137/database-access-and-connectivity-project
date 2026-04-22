#!/usr/bin/env python3
"""
hash_password.py — Print SHA-256 hex of a plain password.
Usage: python3 tools/hash_password.py 'password123'
See workflows/add_user.md for full context.
"""

import hashlib
import sys

if len(sys.argv) != 2:
    print("Usage: python3 tools/hash_password.py '<plain_password>'")
    sys.exit(1)

plain = sys.argv[1]
hashed = hashlib.sha256(plain.encode("utf-8")).hexdigest()
print(hashed)
