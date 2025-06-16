<template>
  <main>
    <Navbar />

    <!-- Table-->
    <div class="container">
      <div class="row">
        <div class="col-md-12">
          <h1 class="text-center">Материалы</h1>
          <!--Add button -->
          <a href="/addProject" class="btn btn-primary">Добавить объект</a>
          <table class="table table-striped">
            <thead>
            <tr>
              <th scope="col">Id</th>
              <th scope="col">Наименование</th>
              <th scope="col">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="project in projects" :key="project.id">
              <th scope="row">{{ project.id }}</th>
              <td>{{ project.name }}</td>
              <td>
                <a class="btn btn-primary" :href="`/editProject/${project.id}`">Edit</a>
                <button class="btn btn-danger mx-2" @click="deleteProject(project.id)">Delete</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onBeforeMount } from 'vue'
import Navbar from '../../components/Navbar.vue'

const projects = ref([])

// Загрузка проектов
const getProjects = async () => {
  try {
    const response = await fetch('http://localhost:8080/projects', {
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    projects.value = await response.json()
    console.log(projects.value)
  } catch (error) {
    console.error('Ошибка при загрузке проектов:', error)
  }
}

// Удаление проекта
const deleteProject = async (id) => {
  try {
    await fetch(`http://localhost:8080/projects/${id}`, {
      method: 'DELETE'
    })
    await getProjects() // Перезагружаем список после удаления
  } catch (error) {
    console.error('Ошибка при удалении проекта:', error)
  }
}

// Загружаем проекты при монтировании компонента
onBeforeMount(getProjects)
</script>