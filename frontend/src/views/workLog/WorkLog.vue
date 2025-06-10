<template>
  <main>
    <Navbar/>
    <div class="container">
      <div class="row">
        <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
          <h1 class="text-center">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-start">
            <a @click="fillInTheLog" class="btn btn-outline-primary mx-3">Сформировать ОЖР</a>
            <a href="#" @click.prevent="generatePdf" class="btn btn-outline-secondary mx-3">Выгрузить в pdf</a>
          </div>
          <div class="table-responsive table-scroll" data-mdb-perfect-scrollbar="true"
               style="position: relative">
            <table class="table table-striped mb-0">
              <thead style="background-color: #002d72;">
              <tr style="color: red;">
                <th class="text-center" scope="col" style="color: black; width: 6%">##</th>
                <th class="text-center" scope="col" style="color: black; width: 7%">Дата</th>
                <th class="text-center" scope="col" style="color: black; width: 50%">Наименование работ,
                  выполняемых...
                </th>
                <th class="text-center" scope="col" style="color: black; width: 15%">Должность, фамилия, инициалы...
                </th>
                <th class="text-center" scope="col" style="color: black; width: 12%">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="workLog in workLogs">
                <td class="text-center" scope="col" style="color: black; width: 6%">{{ workLog.workLogNumber }}</td>
                <td class="text-center" scope="col" style="color: black; width: 7%">{{ workLog.workDate }}</td>
                <td class="text-center" scope="col" style="color: black; width: 50%">{{ workLog.name }}</td>
                <td class="text-center" scope="col" style="color: black; width: 15%">
                  Руководитель работ Трифонов А.Е.
                </td>
                <td style="width: 12%">
                  <a class="btn btn-primary" href="#">Edit</a>
                  <button class="btn btn-danger mx-2">Delete</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'WorkLog',
  components: {
    Navbar
  },

  data() {
    return {
      workLogs: []
    }
  },

  mounted() {
    this.getLogs()
  },

  methods: {
    getLogs() {
      fetch('http://localhost:8080/worklog',{
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.workLogs = data
            this.getLogs()
          })
    },

    fillInTheLog() {
      fetch(`http://localhost:8080/worklog/fill3`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    generatePdf() {
      // Открываем PDF в новой вкладке
      window.open(`http://localhost:8080/worklog/3/pdf`, '_blank');
    }
  },
}
</script>
<style scoped>
html,
body,
.intro {
  height: 100%;
}

table {
  table-layout: fixed;
}


table td,
table th {
  text-overflow: ellipsis;
  overflow: hidden;
  word-wrap: break-word;
}

thead th {
  color: #fff;
}

.card {
  border-radius: .5rem;
}

.table-scroll {
  border-radius: .5rem;
}

.table-scroll table thead th {
  font-size: 1.25rem;
}

thead {
  top: 0;
  position: sticky;
}
</style>
