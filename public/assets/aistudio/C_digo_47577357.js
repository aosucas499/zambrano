// DETECTOR HÍBRIDO BLINDADO
const esElectron = (window.esElectronNativo === true);
let ipcRenderer = window.ipcRenderer || null;

let handleCuaderno = null;     // Usado en Web/Android
let rutaFisicaCuaderno = null; // Usado en Linux (Electron)
let CRITERIOS_MASTER = []; 
let NOTAS_DB = [];         
let CONFIG_SISTEMA = null; 

// CARGA INTELIGENTE MEJORADA (EVITA FALSOS POSITIVOS EN APPIMAGE LIMPIA)
window.addEventListener('DOMContentLoaded', async () => {
  if (!esElectron) return; 
  
  try {
    const historial = await ipcRenderer.invoke('obtener-ultima-ruta');
    let criteriosCargados = false;

    // CORRECCIÓN: Comprobamos estrictamente que 'historial.criterios' exista Y tenga contenido real
    if (historial && historial.criterios && historial.criterios.contenido && historial.criterios.contenido.trim() !== "") {
      procesarTextoCriterios(historial.criterios.contenido);
      document.getElementById('txt-status-crit').className = "text-success small font-monospace";
      document.getElementById('txt-status-crit').innerText = `✅ Criterios EXTERNOS cargados automáticamente del historial.`;
      criteriosCargados = true;
    } else {
      // Si la AppImage está recién creada, el historial estará vacío e irá SIEMPRE por aquí:
      const textoCriteriosInterno = await ipcRenderer.invoke('leer-criterios-interno');
      if (textoCriteriosInterno && textoCriteriosInterno.trim() !== "") {
        procesarTextoCriterios(textoCriteriosInterno);
        document.getElementById('txt-status-crit').className = "text-success small font-monospace";
        document.getElementById('txt-status-crit').innerText = `✅ ${CRITERIOS_MASTER.length} Criterios cargados automáticamente de fábrica.`;
        criteriosCargados = true;
      }
    }

    if (!criteriosCargados) {
      alert("Error crítico: No se pudieron inicializar los criterios de evaluación.");
      return;
    }

    // Evaluamos el cuaderno de notas anterior
    if (historial && historial.cuaderno && historial.cuaderno.contenido) {
      rutaFisicaCuaderno = historial.cuaderno.rutaArchivo;
      procesarContenidoCuaderno(historial.cuaderno.contenido);
      console.log("Cuaderno auto-cargado desde:", rutaFisicaCuaderno);
    } else {
      // Si la AppImage es nueva, habilitamos los botones para empezar de cero
      document.getElementById('btn-criterios').disabled = false;   
      document.getElementById('btn-cuaderno').disabled = false;     
      document.getElementById('btn-crear-vacio').disabled = false;  
      
      document.getElementById('txt-status-cuad').className = "text-primary small font-monospace";
      document.getElementById('txt-status-cuad').innerText = "Haz clic abajo para abrir tu cuaderno de notas o crear uno nuevo.";
    }

  } catch (e) {
    console.error("Error crítico en el arranque de la AppImage:", e);
  }
});

function procesarTextoCriterios(texto) {
  CRITERIOS_MASTER = [];
  Papa.parse(texto, {
    skipEmptyLines: true,
    complete: function(results) {
      let filas = results.data;
      let inicioFila = (filas[0][0] && filas[0][0].toLowerCase().includes("area")) ? 1 : 0;
      
      for(let i = inicioFila; i < filas.length; i++) {
        let f = filas[i];
        if(!f || f.length < 5) continue; 
        let area = f[0] ? f[0].trim() : "";
        let curso = f[2] ? f[2].trim() : "";        
        let codCrit = f[4] ? f[4].trim() : "";      
        let descCrit = f[5] ? f[5].trim() : codCrit; 

        if(area && curso && codCrit) {
          CRITERIOS_MASTER.push({ area, nivel: curso, cod: codCrit, desc: descCrit });
        }
      }
    }
  });
}

async function cargarCriteriosCSV() {
  let texto = "";
  let rutaSeleccionada = "";

  if (esElectron) {
    try {
      const result = await ipcRenderer.invoke('seleccionar-cuaderno-nativo'); 
      if (!result) return;
      texto = result.contenido;
      rutaSeleccionada = result.rutaArchivo;
    } catch (e) {
      console.error(e);
      return;
    }
  } else {
    try {
      const [handleCrit] = await window.showOpenFilePicker({
        types: [{ description: 'Criterios CSV', accept: {'text/csv': ['.csv']} }]
      });
      const file = await handleCrit.getFile();
      texto = await file.text();
    } catch (e) { return; }
  }
  
  if (texto && texto.trim() !== "") {
    procesarTextoCriterios(texto);
    
    if (CRITERIOS_MASTER.length > 0) {
      document.getElementById('txt-status-crit').className = "text-success small font-monospace";
      document.getElementById('txt-status-crit').innerText = `✅ ¡Actualizado! ${CRITERIOS_MASTER.length} criterios externos cargados a mano.`;
      
      // ¡AQUÍ SE GUARDA EN MEMORIA!: Le decimos a Electron que recuerde esta ruta de criterios
      if (esElectron && rutaSeleccionada) {
        await ipcRenderer.invoke('guardar-ultima-ruta', { tipo: 'criterios', ruta: rutaSeleccionada });
      }

      document.getElementById('btn-cuaderno').disabled = false;
      document.getElementById('btn-crear-vacio').disabled = false;
    }
  }
}

function procesarContenidoCuaderno(texto) {
  NOTAS_DB = [];
  let alumnosLocales = [];
  let asigLocales = {};

  Papa.parse(texto, {
    skipEmptyLines: true,
    complete: function(results) {
      let filas = results.data;
      for(let i = 1; i < filas.length; i++) {
        let f = filas[i];
        if(!f || !f[0]) continue;
        let tipo = f[0].trim().toUpperCase();
        if(tipo === "NOTA") {
          NOTAS_DB.push({ alumno: f[1], area: f[2], nivel: f[3], group: f[4], unidad: f[5], actId: f[6], codigo: f[7], valor: f[8] });
        } else if(tipo === "CONFIG_ALUMNO") {
          alumnosLocales.push({ nombre: f[1], nivel: f[2], grupo: f[3] });
        } else if(tipo === "CONFIG_ASIG") {
          if(!asigLocales[f[1]]) asigLocales[f[1]] = [];
          if(!asigLocales[f[1]].includes(f[2])) asigLocales[f[1]].push(f[2]);
        }
      }

      if(alumnosLocales.length > 0) {
        CONFIG_SISTEMA = { areas: Object.keys(asigLocales), asignaciones: asigLocales, alumnos: alumnosLocales };
        inicializarInterfazCuaderno();
      } else {
        document.getElementById('step1').style.display = 'none';
        document.getElementById('step2').style.display = 'block';
      }
    }
  });
}

async function conectarCuadernoCSV() {
  let texto = "";
  
  if (esElectron) {
    try {
      const archivoNativo = await ipcRenderer.invoke('seleccionar-cuaderno-nativo');
      if (!archivoNativo) return;
      rutaFisicaCuaderno = archivoNativo.rutaArchivo;
      texto = archivoNativo.contenido;
      
      await ipcRenderer.invoke('guardar-ultima-ruta', { tipo: 'cuaderno', ruta: rutaFisicaCuaderno });
    } catch(e) { console.error(e); return; }
  } else {
    try {
      [handleCuaderno] = await window.showOpenFilePicker({
        types: [{ description: 'Cuaderno Zambrano CSV', accept: {'text/csv': ['.csv']} }]
      });
      const file = await handleCuaderno.getFile();
      texto = await file.text();
    } catch(e) { console.log("Carga cancelada."); return; }
  }
  
  procesarContenidoCuaderno(texto);
}

function reconstruirContenidoCSV() {
  let contenidoCSV = "TIPO,DATO1,DATO2,DATO3,DATO4,DATO5,DATO6,DATO7,DATO8\n";
  if(CONFIG_SISTEMA) {
    for(let area in CONFIG_SISTEMA.asignaciones) {
      CONFIG_SISTEMA.asignaciones[area].forEach(nivel => {
        contenidoCSV += `CONFIG_ASIG,"${area}","${nivel}"\n`;
      });
    }
    CONFIG_SISTEMA.alumnos.forEach(al => {
      contenidoCSV += `CONFIG_ALUMNO,"${al.nombre}","${al.nivel}","${al.grupo}"\n`;
    });
  }
  NOTAS_DB.forEach(n => {
    contenidoCSV += `NOTA,"${n.alumno}","${n.area}","${n.nivel}","${n.group}","${n.unidad}","${n.actId}","${n.codigo}",${n.valor}\n`;
  });
  return contenidoCSV.replace(/undefined/g, "");
}

async function guardarHaciaCSV() {
  let contenidoCSV = reconstruirContenidoCSV();
  if (esElectron) {
    if(!rutaFisicaCuaderno) return;
    const guardadoExitoso = await ipcRenderer.invoke('guardar-cuaderno-nativo', {
      rutaArchivo: rutaFisicaCuaderno,
      contenido: contenidoCSV
    });
    actualizarBadgeEstado(guardadoExitoso);
  } else {
    if(!handleCuaderno) return;
    try {
      const writable = await handleCuaderno.createWritable();
      await writable.write(contenidoCSV);
      await writable.close();
      actualizarBadgeEstado(true);
    } catch(e) { actualizarBadgeEstado(false); }
  }
}

function actualizarBadgeEstado(exito) {
  const badge = document.getElementById('badge-estado');
  if (exito) {
    badge.className = "badge bg-success p-2 me-2";
    badge.innerText = "💾 Sincronizado";
  } else {
    badge.className = "badge bg-danger p-2 me-2";
    badge.innerText = "❌ Error al guardar";
  }
}

function inicializarInterfazCuaderno() {
  document.getElementById('vista-bienvenida').style.display = 'none';
  document.getElementById('vista-cuaderno').style.display = 'block';
  if (CONFIG_SISTEMA && CONFIG_SISTEMA.areas && CONFIG_SISTEMA.areas.length > 0) {
    document.getElementById('selArea').innerHTML = CONFIG_SISTEMA.areas.map(a => `<option value="${a}">${a}</option>`).join('');
    cambioArea();
  }
}

function cambioArea() {
  const a = document.getElementById('selArea').value;
  const niveles = CONFIG_SISTEMA.asignaciones[a] || [];
  document.getElementById('selNivel').innerHTML = niveles.map(n => `<option value="${n}">${n}</option>`).join('');
  cambioNivel();
}

function cambioNivel() {
  const n = document.getElementById('selNivel').value;
  const grupos = [...new Set(CONFIG_SISTEMA.alumnos.filter(al => al.nivel === n).map(al => al.grupo))].sort();
  document.getElementById('selGrupo').innerHTML = grupos.map(g => `<option value="${g}">${g}</option>`).join('');
  renderizar();
}

function obtenerCriteriosFiltrados(areaApp, nivelApp) {
  let numeroNivel = nivelApp.match(/\d+/);
  let numBuscado = numeroNivel ? numeroNivel[0] : "";

  return CRITERIOS_MASTER.filter(c => {
    let areaCSV = c.area.toLowerCase().trim();
    let buscar = areaApp.toLowerCase().trim();
    if(buscar.includes("conocimiento") || buscar.includes("cono")) {
      buscar = "cono";
    }
    let coincideArea = areaCSV.includes(buscar) || buscar.includes(areaCSV);
    let numeroCSV = c.nivel.match(/\d+/);
    let numCSVStr = numeroCSV ? numeroCSV[0] : "";
    return coincideArea && (numBuscado !== "" && numBuscado === numCSVStr);
  });
}

function renderizar() {
  const a = document.getElementById('selArea').value;
  const n = document.getElementById('selNivel').value;
  const g = document.getElementById('selGrupo').value;
  const u = document.getElementById('selUnidad').value;
  
  if (u === "GLOBAL") {
    document.getElementById('btn-nueva-act').style.display = 'none';
    renderizarGlobal(a, n, g);
    return;
  }
  
  document.getElementById('btn-nueva-act').style.display = 'block';
  const alumnos = CONFIG_SISTEMA.alumnos.filter(x => x.nivel === n && x.grupo === g);
  let notasSA = NOTAS_DB.filter(x => x.area === a && x.nivel === n && x.group === g && x.unidad === u);
  let actividadesID = [...new Set(notasSA.map(x => x.actId))];

  if (actividadesID.length === 0) {
     document.getElementById('head').innerHTML = `<tr><th class="td-nombre">Alumno</th><th>Sin actividades en esta SA</th></tr>`;
     document.getElementById('body').innerHTML = alumnos.map(al => `<tr><td class="td-nombre">${al.nombre}</td><td>—</td></tr>`).join('');
     return;
  }

  let thHtml = `<tr><th class="td-nombre">Alumno</th>`;
  actividadesID.forEach(id => {
     let unaNota = NOTAS_DB.find(x => x.actId === id);
     let nombreLimpioAct = unaNota ? unaNota.actId.split('_')[0] : "Tarea";
     thHtml += `<th>
                  <div class="th-actividad-header">
                    <span>${nombreLimpioAct}</span>
                    <button class="btn-borrar-act" onclick="eliminarActividadLocal('${id}', '${nombreLimpioAct}')" title="Eliminar actividad completa">🗑️</button>
                  </div>
                </th>`;
  });
  document.getElementById('head').innerHTML = thHtml + `</tr>`;
  
  document.getElementById('body').innerHTML = alumnos.map(al => {
    let html = `<tr><td class="td-nombre">${al.nombre}</td>`;
    actividadesID.forEach(id => {
      let notasActIndividual = NOTAS_DB.filter(nota => nota.alumno === al.nombre && nota.actId === id);
      let valores = notasActIndividual.map(v => v.valor);
      html += `<td onclick="evaluarActividad('${al.nombre}','${id}')">${valores.length ? `<div class="nota-badge bg-nota">${valores.join('·')}</div>` : '—'}</td>`;
    });
    return html + `</tr>`;
  }).join('');
}

function renderizarGlobal(area, nivel, grupo) {
  const alumnos = CONFIG_SISTEMA.alumnos.filter(x => x.nivel === nivel && x.grupo === grupo);
  let criteriosArea = obtenerCriteriosFiltrados(area, nivel);

  let competenciesUnicas = [...new Set(criteriosArea.map(c => {
    let match = c.cod.match(/^(\d+)/); return match ? match[1] : null;
  }))].filter(Boolean);

  let th = `<tr><th class="td-nombre">Alumno</th>` + criteriosArea.map(c => `<th title="${c.desc}">${c.cod}</th>`).join('') + `<th class="bg-primary text-white">MEDIA GLOBAL ÁREA</th></tr>`;
  document.getElementById('head').innerHTML = th;

  document.getElementById('body').innerHTML = alumnos.map(al => {
     let html = `<tr><td class="td-nombre">${al.nombre}</td>`;
     let notasPorCompetencia = {}; competenciesUnicas.forEach(comp => { notasPorCompetencia[comp] = []; });

     criteriosArea.forEach(c => {
        let matchComp = c.cod.match(/^(\d+)/); let compPertenece = matchComp ? matchComp[1] : null;
        let notasHistoricas = NOTAS_DB.filter(n => n.alumno === al.nombre && n.area === area && n.codigo === c.cod);
        
        if(notasHistoricas.length > 0) {
           let sum = notasHistoricas.reduce((acc, curr) => acc + parseFloat(curr.valor || 0), 0);
           let mediaDelCriterio = sum / notasHistoricas.length;
           html += `<td><span class="nota-badge bg-media">${mediaDelCriterio.toFixed(2)}</span></td>`;
           if(compPertenece) notasPorCompetencia[compPertenece].push(mediaDelCriterio);
        } else { html += `<td class="text-muted small">—</td>`; }
     });

     let sumaMediasCompetencias = 0, numCompetenciasEvaluadas = 0;
     for(let comp in notasPorCompetencia) {
        let notasDeEstaComp = notasPorCompetencia[comp];
        if(notasDeEstaComp.length > 0) {
           sumaMediasCompetencias += (notasDeEstaComp.reduce((acc, curr) => acc + curr, 0) / notasDeEstaComp.length);
           numCompetenciasEvaluadas++;
        }
     }
     let notaGlobalFinal = numCompetenciasEvaluadas > 0 ? (sumaMediasCompetencias / numCompetenciasEvaluadas).toFixed(2) : "—";
     return html + `<td class="fw-bold fs-6 text-primary">${notaGlobalFinal}</td></tr>`;
  }).join('');
}

function abrirAct() {
  const area = document.getElementById('selArea').value;
  const nivel = document.getElementById('selNivel').value;
  const unidad = document.getElementById('selUnidad').value; 
  const grupo = document.getElementById('selGrupo').value;

  let criterios = obtenerCriteriosFiltrados(area, nivel);
  if(criterios.length === 0) return Swal.fire('Atención', `No hay criterios para "${area}" en "${nivel}"`, 'warning');

  document.getElementById('mB').innerHTML = criterios.map(c => 
    `<div class="form-check border-bottom py-1 small text-start"><input class="form-check-input c-n" type="checkbox" value="${c.cod}" id="c_${c.cod}"><label class="form-check-label" for="c_${c.cod}"><b>${c.cod}</b>: ${c.desc}</label></div>`
  ).join('');
  
  let modalInst = new bootstrap.Modal(document.getElementById('mod'));
  modalInst.show();
  
  document.getElementById('mBtn').onclick = async () => {
    const nom = document.getElementById('nA').value.trim();
    const seleccionados = Array.from(document.querySelectorAll('.c-n:checked')).map(c => c.value);
    
    if (!nom || seleccionados.length === 0) return Swal.fire('Atención', 'Ingresa un nombre y selecciona algún criterio.', 'warning');

    let duplicado = NOTAS_DB.some(x => x.area === area && x.nivel === nivel && x.group === grupo && x.unidad === unidad && x.actId.split('_')[0].toLowerCase() === nom.toLowerCase());
    if (duplicado) return Swal.fire('Duplicado', 'Ya existe una actividad con ese mismo nombre.', 'error');

    let actIdUnico = `${nom}_${Date.now()}`;
    const alumnos = CONFIG_SISTEMA.alumnos.filter(x => x.nivel === nivel && x.grupo === grupo);
    
    alumnos.forEach(al => {
       seleccionados.forEach(codCrit => {
          NOTAS_DB.push({ alumno: al.nombre, area: area, nivel: nivel, group: grupo, unidad: unidad, actId: actIdUnico, codigo: codCrit, valor: "" });
       });
    });

    document.getElementById('nA').value = ""; 
    let modalInst = bootstrap.Modal.getInstance(document.getElementById('mod'));
    if(modalInst) modalInst.hide(); 
    renderizar();
    await guardarHaciaCSV();
  };
}

async function evaluarActividad(alumno, actId) {
  let registros = NOTAS_DB.filter(n => n.alumno === alumno && n.actId === actId);
  let inputsHtml = registros.map(r => `<div class="mb-2 text-start small"><label class="fw-bold text-primary">${r.codigo}</label><input type="number" step="0.01" max="10" min="0" class="v-n form-control form-control-sm" data-codigo="${r.codigo}" value="${r.valor}"></div>`).join('');
  
  const { value: resultado } = await Swal.fire({ 
    title: alumno, html: inputsHtml, showCancelButton: true, confirmButtonText: 'Guardar Notas', 
    preConfirm: () => Array.from(document.querySelectorAll('.v-n')).map(i => ({ codigo: i.dataset.codigo, nota: i.value })) 
  });
  
  if(resultado) {
    resultado.forEach(item => {
      let r = NOTAS_DB.find(n => n.alumno === alumno && n.actId === actId && n.codigo === item.codigo);
      if(r) r.valor = item.nota;
    });
    renderizar();
    await guardarHaciaCSV();
  }
}

let lineasSeleccionadas = 1;
const AREAS_DISP = ["Lengua", "Matemáticas", "Francés", "Inglés", "Conocimiento del Medio", "Educación Física"];
const NIVELES_DISP = ["1º E.P.", "2º E.P.", "3º E.P.", "4º E.P.", "5º E.P.", "6º E.P."];

function setLineas(n) {
  lineasSeleccionadas = n;
  document.getElementById('step2').style.display = 'none';
  document.getElementById('step3').style.display = 'block';
  let html = "";
  AREAS_DISP.forEach(area => {
    html += `<div class="area-box"><h6 class="fw-bold text-primary border-bottom pb-1">${area}</h6><div class="d-flex flex-wrap gap-2 mt-2">`;
    NIVELES_DISP.forEach(nivel => {
      for(let i = 0; i < lineasSeleccionadas; i++) {
        let grupo = ["A", "B", "C", "D"][i];
        let idCheck = `chk_${area.replace(/\s+/g,'')}_${nivel.replace(/\s+/g,'')}_${grupo}`;
        html += `<div class="form-check form-check-inline bg-white border px-2 py-1 rounded small"><input class="form-check-input check-asig" type="checkbox" id="${idCheck}" data-area="${area}" data-nivel="${nivel}" data-grupo="${grupo}"><label class="form-check-label fw-bold" for="${idCheck}">${nivel.split(' ')[0]}${grupo}</label></div>`;
      }
    });
    html += `</div></div>`;
  });
  document.getElementById('listaAreas').innerHTML = html;
}

function pasarAlPasoAlumnos() {
  let checks = document.querySelectorAll('.check-asig:checked');
  if(checks.length === 0) return Swal.fire('Atención', 'Selecciona al menos una clase.', 'warning');
  
  document.getElementById('step3').style.display = 'none';
  document.getElementById('step4').style.display = 'block';
  
  let gruposUnicos = {};
  checks.forEach(c => { gruposUnicos[`${c.dataset.nivel}_${c.dataset.grupo}`] = { nivel: c.dataset.nivel, grupo: c.dataset.grupo }; });
  
  let html = "";
  for(let k in gruposUnicos) {
    let g = gruposUnicos[k];
    html += `<div class="mb-3 p-3 bg-light border rounded"><label class="form-label fw-bold">Alumnos de ${g.nivel} - ${g.grupo}</label><textarea class="form-control txt-alumnos" rows="3" data-nivel="${g.nivel}" data-grupo="${g.grupo}" placeholder="Un nombre por línea..."></textarea></div>`;
  }
  document.getElementById('listaAlumnosCont').innerHTML = html;
}

async function finalizarConfiguracion() {
  let asig = {}, alus = [];
  document.querySelectorAll('.check-asig:checked').forEach(c => {
    if(!asig[c.dataset.area]) asig[c.dataset.area] = [];
    if(!asig[c.dataset.area].includes(c.dataset.nivel)) asig[c.dataset.area].push(c.dataset.nivel);
  });
  
  document.querySelectorAll('.txt-alumnos').forEach(t => {
    t.value.trim().split('\n').forEach(n => {
      if(n.trim()) alus.push({ nombre: n.trim(), nivel: t.dataset.nivel, grupo: t.dataset.grupo });
    });
  });

  CONFIG_SISTEMA = { areas: Object.keys(asig), asignaciones: asig, alumnos: alus };
  inicializarInterfazCuaderno();
  await guardarHaciaCSV();
}

async function agregarNuevoAlumnoInteractivos() {
  const areaActual = document.getElementById('selArea').value;
  const nivelActual = document.getElementById('selNivel').value;
  const grupoActual = document.getElementById('selGrupo').value;

  const { value: nombreAlumno } = await Swal.fire({
    title: 'Añadir Nuevo Alumno',
    text: `El alumno/a se inscribirá automáticamente en ${nivelActual} - Grupo ${grupoActual}`,
    input: 'text',
    inputPlaceholder: 'Escribe apellidos y nombre...',
    showCancelButton: true,
    confirmButtonText: 'Inscribir',
    cancelButtonText: 'Cancelar',
    inputValidator: (value) => {
      if (!value || !value.trim()) return '¡El nombre no puede estar vacío!';
      let existe = CONFIG_SISTEMA.alumnos.some(al => al.nombre.toLowerCase().trim() === value.toLowerCase().trim());
      if (existe) return 'Ya existe un alumn@ con ese nombre en el cuaderno.';
    }
  });

  if (!nombreAlumno) return;
  const nombreLimpio = nombreAlumno.trim();

  CONFIG_SISTEMA.alumnos.push({ nombre: nombreLimpio, nivel: nivelActual, grupo: grupoActual });

  let notasDeLaClase = NOTAS_DB.filter(x => x.area === areaActual && x.nivel === nivelActual && x.group === grupoActual);
  let actividadesYCriteriosExistentes = [];
  let mapeoVistos = new Set();

  notasDeLaClase.forEach(n => {
    let claveUnica = `${n.unidad}|||${n.actId}|||${n.codigo}`;
    if (!mapeoVistos.has(claveUnica)) {
      mapeoVistos.add(claveUnica);
      actividadesYCriteriosExistentes.push({ unidad: n.unidad, actId: n.actId, codigo: n.codigo });
    }
  });

  actividadesYCriteriosExistentes.forEach(item => {
    NOTAS_DB.push({ alumno: nombreLimpio, area: areaActual, nivel: nivelActual, group: grupoActual, unidad: item.unidad, actId: item.actId, codigo: item.codigo, valor: "" });
  });

  CONFIG_SISTEMA.alumnos.sort((a, b) => a.nombre.localeCompare(b.nombre));
  renderizar();
  await guardarHaciaCSV();

  Swal.fire({ title: '¡Operación Exitosa!', text: `${nombreLimpio} ha sido integrado.`, icon: 'success', timer: 2000, showConfirmButton: false });
}

async function eliminarActividadLocal(actIdReal, nombreActividad) {
  const confirmacion = await Swal.fire({
    title: `¿Eliminar "${nombreActividad}"?`,
    text: "Esta acción borrará todas las notas asociadas de forma irreversible.",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: 'Sí, borrar',
    cancelButtonText: 'Cancelar'
  });

  if (!confirmacion.isConfirmed) return;

  try {
    NOTAS_DB = NOTAS_DB.filter(nota => nota.actId !== actIdReal);
    renderizar();
    await guardarHaciaCSV();
    Swal.fire({ title: 'Actividad Eliminada', icon: 'success', timer: 1500, showConfirmButton: false });
  } catch (error) {
    Swal.fire('Error', 'No se pudo borrar: ' + error.message, 'error');
  }
}

// TU FUNCIÓN DE SIEMPRE ADAPTADA CON UN CATCH SEGURO PARA ANDROID
async function crearCuadernoVacioManual() {
  const cabeceraOficial = "TIPO,DATO1,DATO2,DATO3,DATO4,DATO5,DATO6,DATO7,DATO8\n";

  if (esElectron) {
    // --- COMPORTAMIENTO NATIVO EN TU APPIMAGE DE LINUX ---
    try {
      const archivo = await ipcRenderer.invoke('crear-archivo-vacio-dialogo');
      if (archivo) {
        rutaFisicaCuaderno = archivo.rutaArchivo;
        await ipcRenderer.invoke('guardar-ultima-ruta', { tipo: 'cuaderno', ruta: rutaFisicaCuaderno });
        procesarContenidoCuaderno(archivo.contenido);
        lanzaAlertaExito();
      }
    } catch (err) {
      console.error("Error en Linux:", err);
      Swal.fire('Error', 'No se pudo crear en Linux.', 'error');
    }
  } else {
    // --- COMPORTAMIENTO EN ANDROID / WEB ---
    try {
      // 1. Intentamos usar el selector oficial por si es un Chrome de escritorio moderno
      if (window.showSaveFilePicker) {
        handleCuaderno = await window.showSaveFilePicker({
          suggestedName: 'cuaderno de zambrano.csv',
          types: [{ description: 'Archivos CSV', accept: { 'text/csv': ['.csv'] } }]
        });
        const writable = await handleCuaderno.createWritable();
        await writable.write(cabeceraOficial);
        await writable.close();
      } else {
        // 2. ¡TRUCO PARA ANDROID!: Como el móvil no tiene 'showSaveFilePicker', 
        // creamos el archivo en memoria y forzamos a Android a descargarlo en la carpeta "Descargas"
        const blob = new Blob([cabeceraOficial], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.setAttribute("download", "cuaderno de zambrano.csv");
        document.body.appendChild(link);
        link.click(); // Forzamos la descarga nativa de Android
        document.body.removeChild(link);
      }

      // 3. En ambos casos móviles, pasamos los datos al asistente para saltar al Paso 2
      procesarContenidoCuaderno(cabeceraOficial);
      lanzaAlertaExito();

    } catch (err) {
      console.log("Acción cancelada en Android/Web:", err);
    }
  }
}

function lanzaAlertaExito() {
  Swal.fire({
    title: '¡Archivo Inicializado!',
    text: 'Se ha generado tu "cuaderno de zambrano.csv". Selecciona cuántas líneas tiene tu centro para empezar.',
    icon: 'success'
  });
}
