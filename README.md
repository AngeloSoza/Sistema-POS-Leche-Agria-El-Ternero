# Sistema POS & KDS - Leche Agria El Ternero

**Proyecto Final - Fundamentos de Base de Datos**  
**Universidad Americana (UAM) - Ingeniería en Sistemas**

**Autores:**  
* Angelo Gabriel Soza Espinoza (Frontend Android & Integración)  
* Ervin Perez (Backend Spring Boot & Base de Datos)

---

## 📌 Descripción del Proyecto
Este proyecto es un Sistema de Punto de Venta (POS) y Monitor de Cocina (KDS) integral, diseñado para automatizar y optimizar los flujos de trabajo del restaurante "Leche Agria El Ternero". El sistema gestiona en tiempo real la disponibilidad de mesas, la personalización de órdenes, la comunicación directa con la cocina y la facturación.

La arquitectura del proyecto está dividida en un enfoque Monorepo, separando la capa de persistencia (PostgreSQL), la lógica de negocio (API REST en Spring Boot) y la interfaz de usuario (App Nativa en Android con Kotlin).

---

## 🛠️ Arquitectura y Tecnologías

### 1. Base de Datos (PostgreSQL)
Modelo relacional diseñado para garantizar la integridad de los datos transaccionales del restaurante.
* **Entidades Principales:** `mesas`, `categorias`, `productos`, `ordenes`, `detalle_ordenes`.
* **Características:** Uso de llaves foráneas, restricciones de integridad, control de estados por defecto y campos de auditoría (fechas de creación).

### 2. Backend / API REST (Java + Spring Boot)
Motor lógico que sirve los datos a la aplicación móvil.
* **Framework:** Spring Boot.
* **ORM:** Hibernate / Spring Data JPA para el mapeo objeto-relacional.
* **Endpoints:** Controladores REST para la gestión del menú (CRUD), cambios de estado de las órdenes (`PENDIENTE`, `LISTO`, `PAGADO`), estadísticas de ventas diarias y gestión de mesas.

### 3. Frontend / Cliente Móvil (Kotlin + Jetpack Compose)
Aplicación Android nativa con interfaces reactivas.
* **UI/UX:** Diseño inmersivo de borde a borde (Edge-to-Edge) construido 100% en Jetpack Compose.
* **Networking:** Implementación de `Retrofit` para consumo asíncrono de la API mediante Corrutinas.
* **Arquitectura:** Patrón MVVM (Model-View-ViewModel) con flujos de estado (`StateFlow`).
* **Módulos:**
  * **Meseros:** Mapa visual interactivo de mesas, toma de pedidos con personalización dinámica de combos.
  * **Cocina (KDS):** Monitor en tiempo real de tickets pendientes.
  * **Caja Principal:** Control de cuentas, métodos de pago y exportación nativa de recibos físicos en formato PDF mediante `MediaStore`.
  * **Administración:** Gestión del inventario (activación/desactivación de productos y cambios de precios).

