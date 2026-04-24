# Workflow: Bulk Fetch Wikipedia Images

## Objective
For every car in the DB that has no `imageUrl`, call `/api/carimage` and print the results so an admin can decide which to save.

## When to use
- After adding new cars without images
- Before a demo to make sure all cars look good
- Any time someone says "the cars have no pictures"

## Inputs needed
- Server must be running on port 8080 (see `workflows/run_server.md`)

## Steps

1. Confirm the server is running:
   ```bash
   curl -s http://localhost:8080/api/cars | python3 -m json.tool | head -20
   ```

2. Run the fetch tool:
   ```bash
   python3 tools/fetch_images.py
   ```
   This calls `GET /api/cars`, finds cars where `imageUrl` is null or empty, then calls `/api/carimage?make=&model=&year=` for each and prints the results.

3. Review the output. For each car it prints:
   ```
   [ID] 2013 Toyota Camry → https://upload.wikimedia.org/...
   ```

4. To save an image URL permanently, use the admin dashboard Edit modal to paste the URL into the Image URL field — or run a direct UPDATE:
   ```bash
   /usr/local/mysql/bin/mysql -u root -p'<password>' car_dealership -e \
     "UPDATE cars SET image_url='<url>' WHERE id=<id>;"
   ```

## Edge Cases

- **No URL returned for a car** — Wikipedia has no article with an image for that make/model/year. Leave `imageUrl` empty; the app will show the 🚗 placeholder and lazy-fetch on page load.
- **Server not running** — start it first with `workflows/run_server.md`.
- **Wrong image (logo instead of car photo)** — the backend prefers JPEG over PNG. If a PNG logo still shows, it means no JPEG was found. Skip that car.

## Output
Printed list of car IDs → image URLs. Admin manually saves the ones they want.
