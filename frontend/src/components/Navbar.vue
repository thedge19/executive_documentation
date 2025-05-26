<template>
  <main>
    <!--Bootstrap Navbar-->
    <nav class="navbar navbar-expand-lg bg-body-tertiary fixed-top">
      <div class="container-fluid">
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item">
              <a class="nav-link" href="/">Домой</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/projects">Объекты</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/materials">Материалы</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/standards">СП</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/subObjects">Подобъекты</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/works">Работы</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/controls">Входняк</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/workLog3">ОЖР 3 раздел</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/workLog6">ОЖР 6 раздел</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/registry">Реестр</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </main>
</template>
<style scoped>
a:hover {
  color: red;
  background-color: transparent;
  text-decoration: underline;
}

a:active {
  color: yellow;
  background-color: transparent;
  text-decoration: underline;
}
</style>

