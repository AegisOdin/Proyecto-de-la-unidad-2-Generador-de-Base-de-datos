package com.compilador.service;

import com.compilador.model.Atributo;
import com.compilador.model.Tabla;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GeneradorService {

    @Value("${crud.db.host}") private String dbHost;
    @Value("${crud.db.port}") private String dbPort;
    @Value("${crud.db.user}") private String dbUser;
    @Value("${crud.db.password}") private String dbPassword;

    public byte[] generarZip(String baseDatos, Tabla tabla) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            addEntry(zip, "db.php", dbPhp(baseDatos));
            addEntry(zip, "api.php", apiPhp(tabla));
            addEntry(zip, "index.html", indexHtml(tabla));
            addEntry(zip, "app.js", appJs(tabla));
            addEntry(zip, "styles.css", stylesCss());
            addEntry(zip, "README.md", readme(baseDatos, tabla));
            addEntry(zip, "start.bat", startBat());
            addEntry(zip, "start.sh", startSh());
        }
        return baos.toByteArray();
    }

    private void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String dbPhp(String baseDatos) {
        return "<?php\n" +
                "$DB_HOST = '" + dbHost + "';\n" +
                "$DB_PORT = '" + dbPort + "';\n" +
                "$DB_NAME = '" + baseDatos + "';\n" +
                "$DB_USER = '" + dbUser + "';\n" +
                "$DB_PASS = '" + dbPassword + "';\n\n" +
                "function getConnection() {\n" +
                "    global $DB_HOST, $DB_PORT, $DB_NAME, $DB_USER, $DB_PASS;\n" +
                "    $dsn = \"pgsql:host=$DB_HOST;port=$DB_PORT;dbname=$DB_NAME\";\n" +
                "    $pdo = new PDO($dsn, $DB_USER, $DB_PASS);\n" +
                "    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);\n" +
                "    return $pdo;\n" +
                "}\n";
    }

    private String apiPhp(Tabla tabla) {
        String pk = tabla.nombre + "_key";
        StringBuilder cols = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updateSet = new StringBuilder();
        for (int i = 0; i < tabla.atributos.size(); i++) {
            Atributo a = tabla.atributos.get(i);
            if (i > 0) { cols.append(", "); placeholders.append(", "); updateSet.append(", "); }
            cols.append(a.nombreAtributo);
            placeholders.append(":").append(a.nombreAtributo);
            updateSet.append(a.nombreAtributo).append(" = :").append(a.nombreAtributo);
        }

        StringBuilder bind = new StringBuilder();
        for (Atributo a : tabla.atributos) {
            bind.append("        $stmt->bindValue(':").append(a.nombreAtributo).append("', ");
            if (a.esForeignKey || "numeros".equals(a.tipoAtributo)) {
                bind.append("$data['").append(a.nombreAtributo).append("'] === null || $data['").append(a.nombreAtributo).append("'] === '' ? null : (int)$data['").append(a.nombreAtributo).append("']");
            } else {
                bind.append("$data['").append(a.nombreAtributo).append("'] ?? null");
            }
            bind.append(");\n");
        }

        return "<?php\n" +
                "require_once 'db.php';\n" +
                "header('Content-Type: application/json');\n" +
                "header('Access-Control-Allow-Origin: *');\n" +
                "header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');\n" +
                "header('Access-Control-Allow-Headers: Content-Type');\n\n" +
                "if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { exit; }\n\n" +
                "$method = $_SERVER['REQUEST_METHOD'];\n" +
                "$id = $_GET['id'] ?? null;\n" +
                "$TABLE = '" + tabla.nombre + "';\n" +
                "$PK = '" + pk + "';\n\n" +
                "try {\n" +
                "    $pdo = getConnection();\n" +
                "    if ($method === 'GET') {\n" +
                "        if ($id) {\n" +
                "            $stmt = $pdo->prepare(\"SELECT * FROM $TABLE WHERE $PK = :id\");\n" +
                "            $stmt->execute([':id' => $id]);\n" +
                "            echo json_encode($stmt->fetch(PDO::FETCH_ASSOC));\n" +
                "        } else {\n" +
                "            $stmt = $pdo->query(\"SELECT * FROM $TABLE ORDER BY $PK\");\n" +
                "            echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));\n" +
                "        }\n" +
                "    } elseif ($method === 'POST') {\n" +
                "        $data = json_decode(file_get_contents('php://input'), true);\n" +
                "        $stmt = $pdo->prepare(\"INSERT INTO $TABLE (" + cols + ") VALUES (" + placeholders + ") RETURNING $PK\");\n" +
                bind.toString() +
                "        $stmt->execute();\n" +
                "        echo json_encode(['exito' => true, 'id' => $stmt->fetchColumn()]);\n" +
                "    } elseif ($method === 'PUT') {\n" +
                "        $data = json_decode(file_get_contents('php://input'), true);\n" +
                "        $stmt = $pdo->prepare(\"UPDATE $TABLE SET " + updateSet + " WHERE $PK = :id\");\n" +
                bind.toString() +
                "        $stmt->bindValue(':id', (int)$id);\n" +
                "        $stmt->execute();\n" +
                "        echo json_encode(['exito' => true]);\n" +
                "    } elseif ($method === 'DELETE') {\n" +
                "        $stmt = $pdo->prepare(\"DELETE FROM $TABLE WHERE $PK = :id\");\n" +
                "        $stmt->execute([':id' => $id]);\n" +
                "        echo json_encode(['exito' => true]);\n" +
                "    }\n" +
                "} catch (Exception $e) {\n" +
                "    http_response_code(500);\n" +
                "    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);\n" +
                "}\n";
    }

    private String indexHtml(Tabla tabla) {
        StringBuilder ths = new StringBuilder();
        ths.append("<th>").append(tabla.nombre).append("_key</th>");
        for (Atributo a : tabla.atributos) {
            ths.append("<th>").append(a.nombreAtributo).append("</th>");
        }
        ths.append("<th>Acciones</th>");

        StringBuilder fields = new StringBuilder();
        for (Atributo a : tabla.atributos) {
            String inputType = "text";
            if ("numeros".equals(a.tipoAtributo) || a.esForeignKey) inputType = "number";
            else if ("fecha".equals(a.tipoAtributo)) inputType = "date";
            fields.append("      <label>").append(a.nombreAtributo);
            if (a.esForeignKey) fields.append(" <small>(FK -> ").append(a.tablaReferencia).append(")</small>");
            fields.append("</label>\n");
            fields.append("      <input name=\"").append(a.nombreAtributo).append("\" type=\"").append(inputType).append("\" />\n");
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n<head>\n" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <title>CRUD " + tabla.nombre + "</title>\n" +
                "  <link rel=\"stylesheet\" href=\"styles.css\" />\n" +
                "</head>\n<body>\n" +
                "  <div class=\"app\">\n" +
                "    <h1>CRUD: " + tabla.nombre + "</h1>\n\n" +
                "    <form id=\"form\">\n" +
                "      <input type=\"hidden\" name=\"id\" />\n" +
                fields.toString() +
                "      <div class=\"actions\">\n" +
                "        <button type=\"submit\" id=\"btnGuardar\">Crear</button>\n" +
                "        <button type=\"button\" id=\"btnCancelar\" style=\"display:none\">Cancelar</button>\n" +
                "      </div>\n" +
                "    </form>\n\n" +
                "    <table>\n      <thead><tr>" + ths + "</tr></thead>\n      <tbody id=\"tbody\"></tbody>\n    </table>\n" +
                "  </div>\n" +
                "  <script src=\"app.js\"></script>\n" +
                "</body>\n</html>\n";
    }

    private String appJs(Tabla tabla) {
        String pk = tabla.nombre + "_key";

        StringBuilder cells = new StringBuilder();
        cells.append("    tr.innerHTML += `<td>${row.").append(pk).append("}</td>`;\n");
        for (Atributo a : tabla.atributos) {
            cells.append("    tr.innerHTML += `<td>${row.").append(a.nombreAtributo).append(" ?? ''}</td>`;\n");
        }

        StringBuilder fillForm = new StringBuilder();
        for (Atributo a : tabla.atributos) {
            fillForm.append("  form.").append(a.nombreAtributo).append(".value = row.").append(a.nombreAtributo).append(" ?? '';\n");
        }

        StringBuilder buildData = new StringBuilder();
        for (Atributo a : tabla.atributos) {
            buildData.append("  data.").append(a.nombreAtributo).append(" = form.").append(a.nombreAtributo).append(".value || null;\n");
        }

        return "const API = 'api.php';\n" +
                "const PK = '" + pk + "';\n" +
                "const form = document.getElementById('form');\n" +
                "const tbody = document.getElementById('tbody');\n" +
                "const btnGuardar = document.getElementById('btnGuardar');\n" +
                "const btnCancelar = document.getElementById('btnCancelar');\n" +
                "let filasCache = [];\n\n" +
                "async function listar() {\n" +
                "  const r = await fetch(API);\n" +
                "  filasCache = await r.json();\n" +
                "  tbody.innerHTML = '';\n" +
                "  for (const row of filasCache) {\n" +
                "    const tr = document.createElement('tr');\n" +
                cells.toString() +
                "    const td = document.createElement('td');\n" +
                "    const bEdit = document.createElement('button');\n" +
                "    bEdit.textContent = 'Editar';\n" +
                "    bEdit.onclick = () => editar(row);\n" +
                "    const bDel = document.createElement('button');\n" +
                "    bDel.textContent = 'Borrar';\n" +
                "    bDel.onclick = () => borrar(row[PK]);\n" +
                "    td.appendChild(bEdit); td.appendChild(document.createTextNode(' ')); td.appendChild(bDel);\n" +
                "    tr.appendChild(td);\n" +
                "    tbody.appendChild(tr);\n" +
                "  }\n" +
                "}\n\n" +
                "function editar(row) {\n" +
                "  form.id.value = row[PK];\n" +
                fillForm.toString() +
                "  btnGuardar.textContent = 'Actualizar';\n" +
                "  btnCancelar.style.display = 'inline-block';\n" +
                "}\n\n" +
                "async function borrar(id) {\n" +
                "  if (!confirm('Borrar registro ' + id + '?')) return;\n" +
                "  await fetch(API + '?id=' + id, { method: 'DELETE' });\n" +
                "  listar();\n" +
                "}\n\n" +
                "btnCancelar.onclick = () => {\n" +
                "  form.reset(); form.id.value = '';\n" +
                "  btnGuardar.textContent = 'Crear';\n" +
                "  btnCancelar.style.display = 'none';\n" +
                "};\n\n" +
                "form.onsubmit = async (e) => {\n" +
                "  e.preventDefault();\n" +
                "  const data = {};\n" +
                buildData.toString() +
                "  const id = form.id.value;\n" +
                "  const opts = { method: id ? 'PUT' : 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(data) };\n" +
                "  const url = id ? API + '?id=' + id : API;\n" +
                "  const r = await fetch(url, opts);\n" +
                "  const res = await r.json();\n" +
                "  if (!res.exito) { alert('Error: ' + (res.mensaje || 'desconocido')); return; }\n" +
                "  form.reset(); form.id.value = '';\n" +
                "  btnGuardar.textContent = 'Crear';\n" +
                "  btnCancelar.style.display = 'none';\n" +
                "  listar();\n" +
                "};\n\n" +
                "listar();\n";
    }

    private String stylesCss() {
        return "* { box-sizing: border-box; }\n" +
                "body { font-family: 'Segoe UI', sans-serif; background: #1e1e1e; color: #d4d4d4; margin: 0; padding: 24px; }\n" +
                ".app { max-width: 1000px; margin: 0 auto; }\n" +
                "h1 { color: #569cd6; margin-bottom: 24px; }\n" +
                "form { background: #252526; padding: 20px; border-radius: 6px; margin-bottom: 24px; display: grid; gap: 8px; grid-template-columns: 1fr 1fr; }\n" +
                "form label { font-size: 12px; color: #858585; grid-column: span 1; }\n" +
                "form input { background: #1e1e1e; border: 1px solid #3e3e3e; color: #d4d4d4; padding: 6px 10px; border-radius: 4px; font-size: 13px; }\n" +
                "form input:focus { outline: none; border-color: #569cd6; }\n" +
                ".actions { grid-column: span 2; display: flex; gap: 8px; margin-top: 8px; }\n" +
                "button { background: #0e639c; color: #fff; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: 600; }\n" +
                "button:hover { background: #1177bb; }\n" +
                "table { width: 100%; border-collapse: collapse; background: #252526; border-radius: 6px; overflow: hidden; }\n" +
                "th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #3e3e3e; font-size: 13px; }\n" +
                "th { background: #2d2d2d; color: #569cd6; font-weight: 600; }\n" +
                "tr:hover { background: #2a2a2a; }\n";
    }

    private String readme(String baseDatos, Tabla tabla) {
        return "# CRUD generado: " + tabla.nombre + "\n\n" +
                "Aplicacion web autocontenida para hacer CRUD sobre la tabla `" + tabla.nombre + "` de la base `" + baseDatos + "`.\n\n" +
                "## IMPORTANTE\n\n" +
                "**NO abras `index.html` haciendo doble clic.** El navegador bloquea fetch a `api.php` cuando el origen es `file://`.\n" +
                "Tienes que correrlo con un servidor PHP.\n\n" +
                "## Requisitos\n\n" +
                "- PHP 8+ con extension `pdo_pgsql` habilitada en `php.ini`\n" +
                "- PostgreSQL con la base `" + baseDatos + "` ya creada (usa boton \"Ejecutar en BD\" del IDE)\n\n" +
                "## Como correr\n\n" +
                "Windows: doble clic en `start.bat`\n" +
                "Linux/Mac: `./start.sh`\n\n" +
                "Manual:\n\n" +
                "```bash\n" +
                "php -S localhost:8000\n" +
                "```\n\n" +
                "Abre http://localhost:8000 en tu navegador.\n\n" +
                "## Archivos\n\n" +
                "- `db.php` — conexion PDO\n" +
                "- `api.php` — endpoint REST (GET/POST/PUT/DELETE)\n" +
                "- `index.html` — UI\n" +
                "- `app.js` — logica frontend\n" +
                "- `styles.css` — estilos\n" +
                "- `start.bat` / `start.sh` — lanzador servidor PHP\n";
    }

    private String startBat() {
        return "@echo off\r\n" +
                "echo Arrancando servidor PHP en http://localhost:8000\r\n" +
                "echo Abre el navegador en esa direccion. Ctrl+C para detener.\r\n" +
                "start http://localhost:8000\r\n" +
                "php -S localhost:8000\r\n";
    }

    private String startSh() {
        return "#!/bin/bash\n" +
                "echo \"Arrancando servidor PHP en http://localhost:8000\"\n" +
                "echo \"Ctrl+C para detener\"\n" +
                "(sleep 1 && (xdg-open http://localhost:8000 2>/dev/null || open http://localhost:8000 2>/dev/null)) &\n" +
                "php -S localhost:8000\n";
    }
}
