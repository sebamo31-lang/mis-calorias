# Mis Calorías

App para Android que lleva la cuenta de tus calorías del día, con widget en la pantalla de inicio.

## Qué hace

- **Widget**: muestra cuánto llevas hoy, tu meta, una barra de avance y cuánto te queda (o por cuánto te pasaste). El botón **+ Agregar** abre una ventanita para anotar una comida sin abrir la app.
- **Comidas frecuentes**: lo que más registras aparece como botón; con un toque lo agregas de nuevo.
- **Meta diaria** editable (viene en 1.500 kcal). Menú ⋮ → *Meta diaria*.
- **Hora de corte**: como llegas tarde a la casa, lo que comas antes de las 4:00 AM cuenta para el día anterior. Se cambia en menú ⋮ → *Hora de corte del día*.
- **Días anteriores**: con ◀ ▶ revisas otros días y puedes anotar algo que se te olvidó.
- **Borrar**: mantén presionada una comida.
- **Últimos 7 días** con el total de cada uno.
- **Calculadora** (menú ⋮ → *Calcular mis calorías*): con sexo, edad, peso, altura y actividad calcula tu IMC, tu gasto diario y una meta con déficit. Se abre sola la primera vez.
- **Mis alimentos**: guarda tus comidas con sus calorías (botón *Ver todos* o marcando *Guardar en Mis alimentos* al agregar). Al escribir, te sugiere los guardados y completa las calorías.
- **Modo oscuro** activado de entrada; menú ⋮ → *Tema* para cambiar a claro o automático.
- Todo queda guardado en el teléfono; no necesita internet ni cuenta.

## Cómo sacar el APK

### Opción A: GitHub (no necesitas instalar nada pesado)

1. Crea un repositorio nuevo en GitHub (puede ser privado), por ejemplo `mis-calorias`.
2. En esta carpeta, abre una terminal y escribe:
   ```
   git init
   git add .
   git commit -m "Mis Calorias"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/mis-calorias.git
   git push -u origin main
   ```
3. En GitHub entra a la pestaña **Actions**. Se va a compilar sola (tarda unos 3–5 minutos).
4. Cuando salga el ✅, entra a esa ejecución y abajo descarga **MisCalorias-apk**. Es un .zip con `app-debug.apk` adentro.

### Opción B: Android Studio

1. Abre Android Studio → **Open** → elige la carpeta `MisCalorias`.
2. Espera que termine de sincronizar.
3. Menú **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar en el teléfono

1. Pasa `app-debug.apk` al teléfono (cable, WhatsApp a ti mismo, Drive, etc.).
2. Ábrelo. Si Android pregunta, permite **instalar apps de fuentes desconocidas** para esa app.
3. Para el widget: mantén presionada la pantalla de inicio → **Widgets** → **Mis Calorías** → arrástralo donde quieras.

En Huawei, si el widget tarda en cambiar de día, abre la app una vez y se actualiza al tiro.
