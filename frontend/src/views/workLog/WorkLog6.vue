<template>
  <main>
    <Navbar/>
    <div class="container">
      <div class="row">
        <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
          <h1 class="text-center">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-start">
            <button @click="fillInTheLog" class="btn btn-outline-primary mx-3">Сформировать 6 раздел</button>
            <button @click="generatePdf" class="btn btn-outline-secondary mx-3">Выгрузить в pdf</button>
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
              <tr v-for="act in acts" :key="act.id">
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

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../../components/Navbar.vue'

const acts = ref([])

// Получение актов
const getActs = async () => {
  try {
    const response = await fetch('http://localhost:8080/worklog/6', {
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    acts.value = await response.json()
  } catch (error) {
    console.error('Ошибка при загрузке актов:', error)
  }
}

// Формирование раздела
const fillInTheLog = async () => {
  try {
    await fetch('http://localhost:8080/worklog/fill')
    console.log("Запрос отправлен")
    await getActs() // Обновляем список после формирования
  } catch (error) {
    console.error('Ошибка при формировании раздела:', error)
  }
}

// Генерация PDF
const generatePdf = () => {
  window.open('http://localhost:8080/worklog/6/pdf', '_blank')
}

// Загружаем акты при монтировании компонента
onMounted(getActs)
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
