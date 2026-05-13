# Cloud deployment guide

Recommended demo stack:

- Frontend: Netlify
- Backend: Render Web Service
- Database: Aiven for MySQL free tier

## Why Aiven MySQL

The backend already uses MySQL in production mode, so Aiven MySQL avoids a database migration. The free tier is enough for a demo app and supports importing local SQL dumps.

## 1. Create the cloud database

1. Create an Aiven account.
2. Create an Aiven for MySQL service on the free plan.
3. Open the service connection details and copy:
   - host
   - port
   - database name
   - user
   - password
4. Build the JDBC URL:

```text
jdbc:mysql://HOST:PORT/DATABASE?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&sslMode=REQUIRED
```

To import local MySQL data:

```powershell
mysqldump -u root -p library_management > library_management.sql
mysql -h HOST -P PORT -u USER -p --ssl-mode=REQUIRED DATABASE < library_management.sql
```

If you only need demo data, skip the import. The backend seeds sample data when books/readers are empty.

## 2. Deploy the backend to Render

1. Push this repository to GitHub.
2. In Render, create a new Blueprint or Web Service from this repo.
3. If using Web Service manually:
   - Root Directory: `library-management`
   - Runtime: Docker
   - Dockerfile Path: `./Dockerfile`
   - Docker Context: `.`
4. Add environment variables:

```text
DB_URL=jdbc:mysql://HOST:PORT/DATABASE?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&sslMode=REQUIRED
DB_USERNAME=USER
DB_PASSWORD=PASSWORD
CORS_ALLOWED_ORIGIN_PATTERNS=https://YOUR-NETLIFY-SITE.netlify.app
```

After deployment, your backend API base URL is:

```text
https://YOUR-RENDER-SERVICE.onrender.com/api
```

## 3. Deploy the frontend to Netlify

1. Import the same GitHub repo into Netlify.
2. Netlify can read `netlify.toml`, or set manually:
   - Base directory: `library-management-web`
   - Build command: `npm run build`
   - Publish directory: `library-management-web/dist`
3. Add environment variable:

```text
VITE_API_BASE_URL=https://YOUR-RENDER-SERVICE.onrender.com/api
```

4. Deploy.
5. Copy the Netlify URL back to Render's `CORS_ALLOWED_ORIGIN_PATTERNS`, then redeploy the backend.

## Notes

- Render free web services sleep after inactivity, so the first request can be slow.
- Do not use Render free Postgres for this demo database if you need persistence longer than 30 days.
- PlanetScale currently does not provide a free plan.
