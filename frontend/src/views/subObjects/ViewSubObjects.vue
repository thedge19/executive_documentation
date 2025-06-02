<template>
  <main>
    <Navbar/>

    <!-- Table-->
    <div class="container">
      <div class="row">
        <div class="col-md-12">
          <h1 class="text-center">Подобъекты</h1>
          <!--Add button -->
          <a href="/addSubObject" class="btn btn-primary">Добавить подобъект</a>
          <div class="d-flex justify-content-start mt-3">
            <label class="radio mr-1">
              <input type="radio" @change="onChangeProject()" name="add" :value="4"
                     v-model="projectId"
                     checked>
              <span> <i class="fa fa-user"></i> Грушовая </span>
            </label>
            <label class="radio m-lg-auto">
              <input type="radio" @change="onChangeProject()" name="add" :value="5"
                     v-model="projectId">
              <span> <i class="fa fa-plus-circle"></i> Шесхарис </span>
            </label>
          </div>
          <table class="table table-striped">
            <thead>
            <tr>
              <th scope="col">Id</th>
              <th scope="col">Наименование</th>
              <th scope="col">Обозначение</th>
              <th scope="col">Объект</th>
              <th scope="col" style="width:15%">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="subObject in subObjects" :key="subObject.id">
              <th scope="row">{{ subObject.id }}</th>
              <td><a :href="`/works/${subObject.id}`"> {{ subObject.name }}</a></td>
              <td>{{ subObject.title }}</td>
              <td>{{ subObject.project.name }}</td>
              <td>
                <a class="btn btn-primary" :href="`/editSubObject/${subObject.id}`">Edit</a>
                <button class="btn btn-danger mx-2" @click="deleteSubObject(subObject.id)">Delete</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue'

export default {
  name: 'ViewSubObjects',
  components: {
    Navbar
  },
  data() {
    return {
      subObjects: [],
      projectId: 4
    }
  },

  beforeMount() {
    this.getSubObjects()
  },

  methods: {

    onChangeProject() {
      this.getSubObjects()
    },

    getSubObjects() {
      fetch(`http://localhost:8080/subobjects/${this.projectId}`,{
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.subObjects = data
            console.log(data)
          })
    },

    deleteSubObject(id) {
      if (confirm('Вы действительно хотите удалить подобъект?')) {
        fetch(`http://localhost:8080/subobjects/${id}`, {
          method: 'DELETE'
        })
            .then(response => {
              if (!response.ok) {
                throw new Error('Ошибка при удалении');
              }
              return response;
            })
            .then(data => {
              console.log(data);
              this.getWorks();
              // Можно добавить уведомление об успешном удалении
              alert('Подобъект успешно удалён');
            })
            .catch(error => {
              console.error('Ошибка:', error);
              alert('Не удалось удалить подобъект');
            });
      }
    },
  }
}

</script>