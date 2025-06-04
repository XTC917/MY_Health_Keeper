[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/_7UQvaE8)

# Health Keeper

A personal health assistant application.

## Project Structure

- Frontend: Vue.js application deployed on Vercel
- Backend: Spring Boot application
- Database: Supabase (PostgreSQL)

## Containerization

The backend service is containerized using Docker. To run the application:

1. Build and start the containers:
   ```bash
   # 开发环境（使用 ngrok）
   docker-compose up --build

   # 生产环境（使用固定域名/IP）
   SERVER_ADDRESS=https://your-domain.com docker-compose up --build
   ```

2. Stop the containers:
   ```bash
   docker-compose down
   ```

3. View logs:
   ```bash
   docker-compose logs -f
   ```

## Development

### Frontend
The frontend is deployed on Vercel. No additional setup is required.

### Backend
The backend service runs on port 8081. You have two options for exposing it:

1. Development (using ngrok):
   - Install ngrok
   - Run ngrok: `ngrok http 8081`
   - Use the ngrok URL in your frontend configuration

2. Production (using fixed domain/IP):
   - Set the SERVER_ADDRESS environment variable
   - Update your frontend configuration to use this address

## Environment Variables

The application uses the following environment variables:
- Database configuration (Supabase)
- JWT secret
- Supabase configuration
- SERVER_ADDRESS: The public URL of your backend service (default: http://localhost:8081)

These are configured in `src/main/resources/application.properties` and can be overridden using environment variables.
