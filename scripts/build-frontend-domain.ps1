Copy-Item -Path "ruoyi-ui\.env.production.domain" -Destination "ruoyi-ui\.env.production" -Force
Push-Location "ruoyi-ui"
try {
  npm run build:prod
} finally {
  Pop-Location
}
