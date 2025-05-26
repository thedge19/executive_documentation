<template>
  <main>
    <Navbar/>

    <!-- Table-->
    <div class="container">
      <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
        <h1 class="text-center">Входняк</h1>
        <!--Add button -->
        <button @click.prevent="exportExcel" class="btn btn-outline-success my-1 mx-3">Выгрузить журнал</button>
        <button @click.prevent="toPdf" class="btn btn-outline-secondary my-1 mx-3">Выгрузить в pdf</button>
        <table class="table table-striped">
          <thead>
          <tr>
            <th style="width: 5%" scope="col">##</th>
            <th style="width: 7%" scope="col">Дата</th>
            <th scope="col">Материалы</th>
            <th style="width: 30%" scope="col">Документы</th>
            <th scope="col">Автор серта</th>
            <th style="width: 15%" scope="col">ГОСТ, ТУ</th>
            <th style="width: 5%" scope="col" class="text-center">Кол. стр.</th>
            <th scope="col" style="width:10%">Действие</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="control in controls" :key="control.id">
            <td style="width: 5%">{{ control.controlNumber }}</td>
            <td style="width: 7%">{{ control.date }}</td>
            <td>{{ control.materials }}</td>
            <td style="width: 30%">{{ control.documents }}</td>
            <td>{{ control.author }}</td>
            <td style="width: 15%">{{ control.standard }}</td>
            <td style="width: 5%" class="text-center">{{ control.controlSheetNumbers }}</td>
            <td style="width:10%">
              <a class="btn btn-primary" :href="`/editControl/${control.id}`">Edit</a>
              <button class="btn btn-danger mx-2" @click="deleteControl(control.id)" disabled>Delete</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue'

export default {
  name: 'ViewControl',
  components: {
    Navbar
  },
  data() {
    return {
      controls: []
    }
  },

  mounted() {
    this.getControls()
  },

  methods: {
    getControls() {
      fetch('http://localhost:8080/acts/entrance', {
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.controls = data
            console.log(data)
          })
    },

    exportExcel() {
      fetch(`http://localhost:8080/acts/excelControl`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    toPdf() {
      fetch(`http://localhost:8080/toPdf/entranceControlLog`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    deleteControl(id) {
      fetch(`http://localhost:8080/acts/entrance/${id}`, {
        method: 'DELETE'
      })
          .then(data => {
            console.log(data)
            this.getControls()
          })
    }
  }
}

</script>