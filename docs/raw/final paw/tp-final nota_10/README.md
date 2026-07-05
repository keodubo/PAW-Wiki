<p align="center">
  <a href="http://pawserver.it.itba.edu.ar/paw-2024a-07">
    <img src="frontend/src/assets/logo.png" style="width: 15%" alt="PAW-2024A-07">
  </a>
</p>

<h1 align="center">Grupi</h1>
<h3 align="center">Proyecto de Aplicaciones Web - 2024</h3>

## Descripción del proyecto

Grupi es una aplicación web que permite a las empresas publicar sus productos y crear pools o grupos de gente interesados en dicho producto,
para comprar a precio mayorista y pagar así un precio unitario más bajo.

## Integrantes

* [Federico Inti Garcia Lauberer](https://github.com/intilauberer) - **Legajo: 61374**
* [Felipe Mindlin](https://github.com/felipemindlin) - **Legajo: 62774**
* [Francisco Quian Blanco](https://github.com/PakiQuian) - **Legajo: 63006**
* [Theo Stanfield](https://github.com/stonefeld) - **Legajo: 63403**

## Dependencias

* Java **21**
* Maven **3.9.7**
* Tomcat **9.0.87**
* PostgreSQL **16.2**

## Compilación

Para compilar el proyecto, se debe ejecutar el siguiente comando en la raíz del proyecto:

```bash
mvn clean package
```

Las tablas de la base de datos, **se generan aparte del aplicativo**. Para realizarlo se debe ejecutar el siguiente comando en la raíz del proyecto:

```bash
DEBUG=false ./migrate.sh
```

Dicho script busca las credenciales de la base de datos de producción en el archivo `webapp/src/main/resources/config/production.properties`,
las cuales deben estar dispuestas de la siguiente manera:

```
db.url=jdbc:postgresql://<host>/<database>
db.host=<host>
db.database=<database>
db.port=<port>
db.username=<username>
db.password=<password>
```

En caso de fallar el script de migración, se puede ejecutar los scripts de **SQL** ubicados en la carpeta `migrations` (en orden lógico) por línea de comandos:

```bash
psql -h <host> -U <username> -d <database> -f migrations/<migration>.sql
```

Una vez realizado todo esto, se puede proceder a realizar el *deployment* del aplicativo en un servidor **Tomcat**.

## Credenciales de acceso

Usuario de empresa:

- **Email:** `fquianblanco@itba.edu.ar`
- **Contraseña:** `1234`

Usuario de cliente:

- **Email:** `felipemindlin@gmail.com`
- **Contraseña:** `1234`

Usuario administrador:

- **Email:** `groupbysharing@gmail.com`
- **Contraseña:** `grupi2024`
