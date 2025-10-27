from flask import Flask, jsonify, render_template
import requests
from collections import Counter
import re

# --- Configuración ---
app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

HISTORIAL_API_URL = 'http://localhost:8080/api/historial'

# --- CONSTANTES PARA EL ANÁLISIS ---
# Si un recurso se reserva más de este número de veces, se considera de "alta demanda".
# Puedes ajustar este valor según tus necesidades.
UMBRAL_ALTA_DEMANDA = 15

# --- Lógica Central ---

def _obtener_y_procesar_datos():
    """Función interna para obtener y procesar los datos."""
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

    # Devolvemos el historial completo para el análisis mensual
    return contador_salas, contador_articulos, historial, None, 200

def _generar_texto_resumen(contador_salas, contador_articulos, historial):
    """Genera el texto del informe, ahora con recomendaciones y análisis mensual."""
    total_reservas = len(historial)
    if total_reservas == 0:
        return "No hay datos históricos suficientes para generar un informe."
    try:
        sala_top_nombre, sala_top_cant = contador_salas.most_common(1)[0]
        articulo_top_nombre, articulo_top_cant = contador_articulos.most_common(1)[0]
    except IndexError:
        return "No hay datos suficientes para determinar los elementos más populares."

    # --- 1. Construcción del Resumen Básico ---
    resumen_texto = (
        f"Análisis y Predicción de Uso de Recursos\n"
        f"=========================================\n\n"
        f"Resumen del Año Anterior:\n"
        f"-------------------------\n"
        f"Se analizaron un total de {total_reservas} reservas.\n"
        f"La sala más solicitada fue '{sala_top_nombre}' con {sala_top_cant} reservas.\n"
        f"El artículo más popular fue '{articulo_top_nombre}' con {articulo_top_cant} usos.\n\n"
        f"Predicción de Demanda (Top 5):\n"
        f"--------------------------------\n"
    )
    resumen_texto += f"Salas con mayor demanda proyectada:\n"
    for nombre, cant in contador_salas.most_common(5):
        resumen_texto += f"- {nombre} (Aprox. {cant} reservas)\n"
    resumen_texto += f"\nArtículos con mayor demanda proyectada:\n"
    for nombre, cant in contador_articulos.most_common(5):
        resumen_texto += f"- {nombre} (Aprox. {cant} usos)\n"

    # --- 2. Análisis de Estacionalidad (Meses) ---
    meses_reservas = [item['fechaHoraInicio'][5:7] for item in historial]
    contador_meses = Counter(meses_reservas)
    mapa_meses = {
        '01': 'Enero', '02': 'Febrero', '03': 'Marzo', '04': 'Abril',
        '05': 'Mayo', '06': 'Junio', '07': 'Julio', '08': 'Agosto',
        '09': 'Septiembre', '10': 'Octubre', '11': 'Noviembre', '12': 'Diciembre'
    }

    resumen_texto += (
        f"\nAnálisis de Estacionalidad:\n"
        f"---------------------------\n"
        f"Los meses con mayor concentración de reservas fueron:\n"
    )
    for mes_num, cant in contador_meses.most_common(3):
        resumen_texto += f"- {mapa_meses.get(mes_num, 'Desconocido')}: {cant} reservas.\n"
    resumen_texto += "Se recomienda prestar especial atención a la disponibilidad de recursos durante estos periodos.\n"

    # --- 3. Generación de Recomendaciones ---
    recomendaciones = []
    for sala, cant in contador_salas.items():
        if cant > UMBRAL_ALTA_DEMANDA:
            recomendaciones.append(f"La sala '{sala}' presenta una alta demanda ({cant} reservas). Considere optimizar su gestión o evaluar una posible expansión.")

    for articulo, cant in contador_articulos.items():
        if cant > UMBRAL_ALTA_DEMANDA:
            recomendaciones.append(f"El artículo '{articulo}' fue solicitado {cant} veces. Se recomienda adquirir más unidades para evitar escasez en picos de demanda.")

    resumen_texto += (
        f"\nRecomendaciones Accionables:\n"
        f"----------------------------\n"
    )
    if not recomendaciones:
        resumen_texto += "El uso de los recursos se mantiene dentro de los parámetros normales. No se identifican acciones prioritarias en este momento."
    else:
        for rec in recomendaciones:
            resumen_texto += f"- {rec}\n"

    return resumen_texto

# --- Endpoints de la API (sin cambios en su lógica de llamada) ---

@app.route('/prediccion/mas-populares', methods=['GET'])
def predecir_elementos_populares():
    contador_salas, contador_articulos, _, error, status_code = _obtener_y_procesar_datos()
    if error: return jsonify(error), status_code
    resultado = {
        'salas_populares': [{'nombre': nombre, 'cantidad_reservas': cant} for nombre, cant in contador_salas.most_common(5)],
        'articulos_populares': [{'nombre': nombre, 'cantidad_reservas': cant} for nombre, cant in contador_articulos.most_common(5)]
    }
    return jsonify(resultado)

@app.route('/prediccion/resumen-anual', methods=['GET'])
def predecir_resumen_anual_json():
    contador_salas, contador_articulos, historial, error, status_code = _obtener_y_procesar_datos()
    if error: return jsonify(error), status_code
    texto = _generar_texto_resumen(contador_salas, contador_articulos, historial)
    return jsonify({'resumen': texto})

@app.route('/informe-prediccion', methods=['GET'])
def ver_informe_prediccion_html():
    contador_salas, contador_articulos, historial, error, status_code = _obtener_y_procesar_datos()
    if error:
        return render_template('informe.html', texto_del_informe=error['error']), status_code

    texto_informe = _generar_texto_resumen(contador_salas, contador_articulos, historial)

    return render_template('informe.html', texto_del_informe=texto_informe)

# --- Ejecución del Servidor ---

if __name__ == '__main__':
    print("Iniciando servicio de predicción en http://localhost:5001")
    app.run(port=5001, debug=True)