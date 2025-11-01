from flask import Flask, jsonify, render_template, url_for
import requests
from collections import Counter
import re
import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns

# NUEVO: Imports para el análisis predictivo
import pandas as pd
from sklearn.linear_model import LinearRegression
from matplotlib.dates import MonthLocator, DateFormatter

# --- Configuración (sin cambios) ---
app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False
HISTORIAL_API_URL = 'http://localhost:8080/api/historial'
UMBRAL_ALTA_DEMANDA = 15

# --- Lógica de Gráficos (Existente) ---
def _generar_grafico_populares(contador_salas, contador_articulos):
    # ... (Esta función se mantiene exactamente igual que antes)
    if not contador_salas and not contador_articulos:
        return None
    salas_top_5 = contador_salas.most_common(5)
    articulos_top_5 = contador_articulos.most_common(5)
    nombres_salas = [item[0] for item in salas_top_5]
    cantidades_salas = [item[1] for item in salas_top_5]
    nombres_articulos = [item[0] for item in articulos_top_5]
    cantidades_articulos = [item[1] for item in articulos_top_5]
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 12))
    sns.set_theme(style="whitegrid")
    sns.barplot(x=cantidades_salas, y=nombres_salas, ax=ax1, palette="viridis", hue=nombres_salas, legend=False)
    ax1.set_title('Top 5 Salas Más Solicitadas', fontsize=16)
    ax1.set_xlabel('Cantidad de Reservas')
    ax1.set_ylabel('Sala')
    sns.barplot(x=cantidades_articulos, y=nombres_articulos, ax=ax2, palette="plasma", hue=nombres_articulos, legend=False)
    ax2.set_title('Top 5 Artículos Más Solicitados', fontsize=16)
    ax2.set_xlabel('Cantidad de Usos')
    ax2.set_ylabel('Artículo')
    plt.tight_layout(pad=3.0)
    output_dir = os.path.join('static', 'images')
    os.makedirs(output_dir, exist_ok=True)
    ruta_relativa_grafico = 'images/reporte_popularidad.png'
    ruta_completa_grafico = os.path.join('static', ruta_relativa_grafico)
    plt.savefig(ruta_completa_grafico)
    plt.close(fig)
    return ruta_relativa_grafico

# --- NUEVO: Lógica para el Gráfico de Predicción ---
def _generar_grafico_prediccion(historial):
    """Genera un gráfico de tendencia de reservas mensuales usando regresión lineal."""
    if not historial or len(historial) < 2: # Necesitamos al menos 2 puntos para una tendencia
        return None

    # 1. Preparar los datos con Pandas
    df = pd.DataFrame(historial)
    df['fechaHoraInicio'] = pd.to_datetime(df['fechaHoraInicio'])

    # Agrupamos las reservas por mes
    reservas_mensuales = df.set_index('fechaHoraInicio').resample('ME').size()

    if len(reservas_mensuales) < 2:
        return None

    # 2. Preparar datos para el modelo de Regresión Lineal
    # Convertimos las fechas a un formato numérico (número de días desde el inicio)
    X = (reservas_mensuales.index - reservas_mensuales.index.min()).days.values.reshape(-1, 1)
    y = reservas_mensuales.values

    # 3. Entrenar el modelo
    model = LinearRegression()
    model.fit(X, y)
    tendencia_predicha = model.predict(X)

    # 4. Crear el gráfico
    plt.style.use('seaborn-v0_8-whitegrid')
    fig, ax = plt.subplots(figsize=(12, 7))

    # Gráfico de barras para las reservas reales
    ax.bar(reservas_mensuales.index, y, width=20, alpha=0.7, label='Reservas Reales por Mes', color='#3498db')

    # Gráfico de línea para la predicción de tendencia
    ax.plot(reservas_mensuales.index, tendencia_predicha, color='#e74c3c', linestyle='--', linewidth=2, label='Línea de Tendencia (Predicción)')

    # Formatear el gráfico
    ax.set_title('Predicción de Tendencia de Reservas Mensuales', fontsize=18, pad=20)
    ax.set_ylabel('Cantidad de Reservas')
    ax.set_xlabel('Mes')
    ax.legend()

    # Formatear el eje X para mostrar los meses claramente
    ax.xaxis.set_major_locator(MonthLocator())
    ax.xaxis.set_major_formatter(DateFormatter('%b %Y')) # Ej: 'Ene 2024'
    fig.autofmt_xdate()

    # 5. Guardar el gráfico
    output_dir = os.path.join('static', 'images')
    os.makedirs(output_dir, exist_ok=True)
    ruta_relativa_grafico = 'images/prediccion_tendencia.png'
    ruta_completa_grafico = os.path.join('static', ruta_relativa_grafico)
    plt.savefig(ruta_completa_grafico)
    plt.close(fig)

    return ruta_relativa_grafico

# --- Lógica Central y de Texto (sin cambios) ---
def _obtener_y_procesar_datos():
    # ... (Esta función se mantiene exactamente igual)
    try:
        response = requests.get(HISTORIAL_API_URL, timeout=10)
        response.raise_for_status()
        historial = response.json()
    except requests.exceptions.RequestException as e:
        print(f"ERROR: No se pudo conectar al servicio Java. {e}")
        return None, None, None, {'error': 'El servicio de datos (Java) no está disponible.'}, 503
    if not historial:
        return Counter(), Counter(), [], None, 200
    nombres_salas = [re.split(r'\s*\(', item['salaInfo'])[0] for item in historial]
    contador_salas = Counter(nombres_salas)
    lista_articulos = []
    for item in historial:
        articulos_str = item.get('articulosInfo', '')
        if articulos_str and articulos_str != 'Ninguno':
            articulos_individuales = [articulo.strip() for articulo in articulos_str.split(',')]
            lista_articulos.extend(articulos_individuales)
    contador_articulos = Counter(lista_articulos)
    return contador_salas, contador_articulos, historial, None, 200

def _generar_texto_resumen(contador_salas, contador_articulos, historial):
    # ... (Esta función se mantiene exactamente igual)
    total_reservas = len(historial)
    if total_reservas == 0:
        return "No hay datos históricos suficientes para generar un informe."
    try:
        sala_top_nombre, sala_top_cant = contador_salas.most_common(1)[0]
        articulo_top_nombre, articulo_top_cant = contador_articulos.most_common(1)[0]
    except IndexError:
        return "No hay datos suficientes para determinar los elementos más populares."
    resumen_texto = (f"Análisis y Predicción de Uso de Recursos\n" f"=========================================\n\n" f"Resumen del Año Anterior:\n" f"-------------------------\n" f"Se analizaron un total de {total_reservas} reservas.\n" f"La sala más solicitada fue '{sala_top_nombre}' con {sala_top_cant} reservas.\n" f"El artículo más popular fue '{articulo_top_nombre}' con {articulo_top_cant} usos.\n\n" f"Predicción de Demanda (Top 5):\n" f"--------------------------------\n")
    resumen_texto += f"Salas con mayor demanda proyectada:\n"
    for nombre, cant in contador_salas.most_common(5):
        resumen_texto += f"- {nombre} (Aprox. {cant} reservas)\n"
    resumen_texto += f"\nArtículos con mayor demanda proyectada:\n"
    for nombre, cant in contador_articulos.most_common(5):
        resumen_texto += f"- {nombre} (Aprox. {cant} usos)\n"
    meses_reservas = [item['fechaHoraInicio'][5:7] for item in historial]
    contador_meses = Counter(meses_reservas)
    mapa_meses = {'01': 'Enero', '02': 'Febrero', '03': 'Marzo', '04': 'Abril', '05': 'Mayo', '06': 'Junio', '07': 'Julio', '08': 'Agosto', '09': 'Septiembre', '10': 'Octubre', '11': 'Noviembre', '12': 'Diciembre'}
    resumen_texto += (f"\nAnálisis de Estacionalidad:\n" f"---------------------------\n" f"Los meses con mayor concentración de reservas fueron:\n")
    for mes_num, cant in contador_meses.most_common(3):
        resumen_texto += f"- {mapa_meses.get(mes_num, 'Desconocido')}: {cant} reservas.\n"
    resumen_texto += "Se recomienda prestar especial atención a la disponibilidad de recursos durante estos periodos.\n"
    recomendaciones = []
    for sala, cant in contador_salas.items():
        if cant > UMBRAL_ALTA_DEMANDA:
            recomendaciones.append(f"La sala '{sala}' presenta una alta demanda ({cant} reservas). Considere optimizar su gestión o evaluar una posible expansión.")
    for articulo, cant in contador_articulos.items():
        if cant > UMBRAL_ALTA_DEMANDA:
            recomendaciones.append(f"El artículo '{articulo}' fue solicitado {cant} veces. Se recomienda adquirir más unidades para evitar escasez en picos de demanda.")
    resumen_texto += (f"\nRecomendaciones Accionables:\n" f"----------------------------\n")
    if not recomendaciones:
        resumen_texto += "El uso de los recursos se mantiene dentro de los parámetros normales. No se identifican acciones prioritarias en este momento."
    else:
        for rec in recomendaciones:
            resumen_texto += f"- {rec}\n"
    return resumen_texto

# --- Endpoints (Solo se modifica el del informe HTML) ---
@app.route('/prediccion/mas-populares', methods=['GET'])
def predecir_elementos_populares():
    # ... (sin cambios)
    contador_salas, contador_articulos, _, error, status_code = _obtener_y_procesar_datos()
    if error: return jsonify(error), status_code
    resultado = {'salas_populares': [{'nombre': nombre, 'cantidad_reservas': cant} for nombre, cant in contador_salas.most_common(5)], 'articulos_populares': [{'nombre': nombre, 'cantidad_reservas': cant} for nombre, cant in contador_articulos.most_common(5)]}
    return jsonify(resultado)

@app.route('/prediccion/resumen-anual', methods=['GET'])
def predecir_resumen_anual_json():
    # ... (sin cambios)
    contador_salas, contador_articulos, historial, error, status_code = _obtener_y_procesar_datos()
    if error: return jsonify(error), status_code
    texto = _generar_texto_resumen(contador_salas, contador_articulos, historial)
    return jsonify({'resumen': texto})

@app.route('/informe-prediccion', methods=['GET'])
def ver_informe_prediccion_html():
    contador_salas, contador_articulos, historial, error, status_code = _obtener_y_procesar_datos()
    if error:
        return render_template('informe.html', texto_del_informe=error['error'], ruta_grafico=None, ruta_grafico_prediccion=None), status_code

    texto_informe = _generar_texto_resumen(contador_salas, contador_articulos, historial)

    # Generamos el primer gráfico (descriptivo)
    ruta_grafico_populares = _generar_grafico_populares(contador_salas, contador_articulos)

    # NUEVO: Generamos el segundo gráfico (predictivo)
    ruta_grafico_prediccion = _generar_grafico_prediccion(historial)

    # Pasamos AMBAS rutas a la plantilla
    return render_template('informe.html',
                           texto_del_informe=texto_informe,
                           ruta_grafico_populares=ruta_grafico_populares,
                           ruta_grafico_prediccion=ruta_grafico_prediccion)

# --- Ejecución del Servidor (sin cambios) ---
if __name__ == '__main__':
    print("Iniciando servicio de predicción en http://localhost:5001")
    app.run(port=5001, debug=True)