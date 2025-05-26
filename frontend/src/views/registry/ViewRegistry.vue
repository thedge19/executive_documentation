<template>
  <main>
    <Navbar/>
    <div class="container">
      <div class="row">
        <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
          <h1 class="text-center">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-start mt-2">
            <label class="m-lg-2 mt-2">Добавить акт</label>
            <select v-if="month === ''" class="form-select m-lg-2" style="width: 7%" id="inputGroupSelect02"
                    v-model="month">
              <option selected/>
              <option v-for="month in months">{{ month }}</option>
            </select>
            <label v-if="month !== ''" class="m-lg-2"> Месяц: {{ month }} </label>
            <select class="form-select m-lg-2" style="width: 7%" id="inputGroupSelect02" v-model="actId">
              <option selected/>
              <option v-for="act in acts" :value="act.id">
                Акт освидетельствования скрытых работ №{{ act.actNumber }} от {{ act.date }} "{{ act.works }}"
              </option>
            </select>
            <button @click.prevent="addRegistryRows" class="btn btn-primary btn-block confirm-button m-lg-2">Добавить
              строки
            </button>
            <button @click.prevent="createRegistry" class="btn btn-outline-dark btn-block confirm-button m-lg-2">
              Сформировать реестр
            </button>

          </div>
          <div class="table-responsive table-scroll" data-mdb-perfect-scrollbar="true"
               style="position: relative">
            <table class="table table-striped mb-0">
              <thead style="background-color: #002d72;">
              <tr style="color: red;">
                <th class="text-center" scope="col" style="color: black; width: 3%">##</th>
                <th class="text-center" scope="col" style="color: black; width: 35%">Наименование документа
                </th>
                <th class="text-center" scope="col" style="color: black; width: 15%"># чертежа, акта...</th>
                <th class="text-center" scope="col" style="color: black; width: 10%">Организация, составившая...</th>
                <th class="text-center" scope="col" style="color: black; width: 7%">Кол-во листов
                </th>
                <th class="text-center" scope="col" style="color: black; width: 7%">Стр. по списку</th>
                <th class="text-center" scope="col" style="color: black; width: 12%">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="registry in registries">
                <td class="text-center" style="width: 3%">{{ registry.rowNumber }}</td>
                <td class="text-center" style="width: 35%">{{ registry.documentName }}</td>
                <td class="text-center" style="width: 15%">
                  {{ registry.documentNumber + " от " + registry.documentDate + "г." }}
                </td>
                <td class="text-center" style="width: 10%">{{ registry.documentAuthor }}</td>
                <td class="text-center" style="width: 7%">{{ registry.numberOfSheets }}</td>
                <td class="text-center" style="width: 7%">{{ registry.listInOrder }}</td>
                <td class="text-center" style="width: 12%">
                  <div
                      :class="registry.documentName.startsWith('Акт освидетельствования')
                      || registry.documentName.startsWith('Реестр')
                      || registry.documentName.startsWith('Журнал')
                      || registry.documentName.startsWith('Общий') === true ? '' : 'visually-hidden'">
                    <a class="btn btn-primary" :href="`/editRegistry/${registry.id}`">
                      Edit</a>
                    <button class="btn btn-danger mx-2" @click="deleteRow(registry.id)">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
            <button @click="getSomething" class="btn btn-outline-primary mt-3">
              Жми
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'Home',
  components: {
    Navbar
  },

  data() {
    return {

      blocked: false,

      months: [
        'Январь',
        'Февраль',
        'Март',
        'Апрель',
        'Май',
        'Июнь',
        'Июль',
        'Август',
        'Сентябрь',
        'Октябрь',
        'Ноябрь',
        'Декабрь'
      ],

      monthsMap: new Map([
        ['Январь', 1],
        ['Февраль', 2],
        ['Март', 3],
        ['Апрель', 4],
        ['Май', 5],
        ['Июнь', 6],
        ['Июль', 7],
        ['Август', 8],
        ['Сентябрь', 9],
        ['Октябрь', 10],
        ['Ноябрь', 11],
        ['Декабрь', 12]
      ]),
      acts: [],
      registries: [],
      errors: [],
      month: 'Март',
      actId: '',
      totalRegistry: []
    }
  },

  mounted() {
    this.getActs()
    this.getRegistries()
  },

  methods: {

    getSomething() {

    },

    getActs() {
      fetch('http://localhost:8080/acts/nullInRegistries',{
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.acts = data
            console.log(this.actId)
          })
    },


    getRegistries() {
      fetch(`http://localhost:8080/registries/${this.monthsMap.get(this.month)}`,)
          .then(res => res.json())
          .then(data => {
            this.registries = data
            console.log(data)
          })
    },

    addRegistryRows() {
      fetch(`http://localhost:8080/registries`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          actId: this.actId,
          monthId: this.monthsMap.get(this.month),
        })
      }).then(data => (
          this.getRegistries()
      )).then(data => {
        this.getActs()
      })
    },

    deleteRow(id) {
      fetch(`http://localhost:8080/registries/${id}`, {
        method: 'DELETE'
      })
          .then(data => {
            console.log(data)
            this.getRegistries()
          })
    },

    pdfExport(monthId) {
      fetch(`http://localhost:8080/toPdf/${monthId}`,)
          .then(data => {
            console.log(data)
            this.getRegistries()
          })
    },

    workLog() {
      fetch(`http://localhost:8080/toPdf/workLog/1`)
          .then(data => {
            console.log("Запрос отправлен")
          })
    },

    createRegistry() {
      fetch(`http://localhost:8080/main/${this.monthsMap.get(this.month)}`)
          .then(data => {
            console.log(data)
            this.getRegistries()
          })
    }
  },
}

</script>
<style scoped>
html,
body,
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
