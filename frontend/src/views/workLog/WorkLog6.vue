<template>
  <main>
    <Navbar/>
    <div class="container">
      <div class="row">
        <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
          <h1 class="text-center">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-start">
            <a @click="fillInTheLog" class="btn btn-outline-primary mx-3">Сформировать 6 раздел</a>
            <a @click="toExcel6" class="btn btn-outline-success mx-3">Выгрузить в эксель</a>
            <a @click="toPdf6" class="btn btn-outline-secondary mx-3">Выгрузить в pdf</a>
          </div>
          <div class="table-responsive table-scroll" data-mdb-perfect-scrollbar="true"
               style="position: relative">
            <table class="table table-striped mb-0">
              <thead style="background-color: #002d72;">
              <tr style="color: red;">
                <th class="text-center" scope="col" style="color: black; width: 50%">Наименование исполнительной...</th>
                <th class="text-center" scope="col" style="color: black; width: 15%">Дата подписания акта...</th>
                <th class="text-center" scope="col" style="color: black; width: 12%">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="act in acts">
                <td class="text-center" style="color: black; width: 6%">Акт освидетельствования скрытых работ № {{ act.actNumber }} {{ act.works }}</td>
                <td class="text-center" style="color: black; width: 50%">{{ act.endDate }}</td>
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
  name: 'WorkLog6',
  components: {
    Navbar
  },

  data() {
    return {
      acts: []
    }
  },

  mounted() {
    this.getActs()
  },

  methods: {
    getActs() {
      fetch('http://localhost:8080/worklog/6',{
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.acts = data
            this.getActs()
          })
    },
    // deleteAct(id) {
    //   fetch(`http://localhost:8080/acts/${id}`, {
    //     method: 'DELETE'
    //   })
    //       .then(data => {
    //         console.log(data)
    //         this.getActs()
    //       })
    // },

    fillInTheLog() {
      fetch(`http://localhost:8080/worklog/fill`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    toExcel6() {
      fetch(`http://localhost:8080/worklog/excel6`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    toPdf6() {
      fetch(`http://localhost:8080/toPdf/workLog6`)
          .then(data => {
            console.log("Запрос отправлен")
          })
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
