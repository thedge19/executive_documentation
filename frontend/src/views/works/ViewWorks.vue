<template>
  <main>
    <Navbar/>

    <!-- Table-->
    <div class="container">
      <div class="row">
        <div class="col-md-24">
          <h1 class="text-center">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-between h5">
            <a :href="`/addWork/${subObjectId}`" class="btn btn-primary py-3">Добавить работу</a>
            <div class="input-group mb-3 mt-3" style="width: 50%">
              <label class="input-group-text" for="inputGroupSelect01">Подобъекты</label>
              <select class="form-select" id="inputGroupSelect01"
                      v-model="subObjectId" @change="onChangeSubObject()">
                <option selected>Choose...</option>
                <option style="width: min-content"
                        v-for="subObject in subObjects" :value="subObject.id">{{ subObject.name }}
                </option>
              </select>
            </div>
          </div>
          <table class="table table-striped" style="width:100%">
            <thead>
            <tr>
              <th scope="col">Id</th>
              <th scope="col">Наименование</th>
              <th scope="col">Ед. изм.</th>
              <th scope="col">Количество</th>
              <th scope="col">Выполнено</th>
              <th scope="col">Осталось</th>
              <th scope="col">Подобъект</th>
              <th scope="col" style="width:15%">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="work in works" :key="work.id">
              <th scope="row">{{ work.id }}</th>
              <td>{{ work.name }}</td>
              <td>{{ work.units }}</td>
              <td>{{ work.quantity }}</td>
              <td>{{ work.done }}</td>
              <td>{{ work.finalQuantity }}</td>
              <td>{{ work.subObject.title }}</td>
              <td>
                <a class="btn btn-primary" :href="`/editWork/${work.id}`">Edit</a>
                <button class="btn btn-danger mx-2" @click="deleteWork(work.id)">Delete</button>
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
  name: 'ViewWorks',
  components: {
    Navbar
  },
  data() {
    return {
      works: [],
      subObjects: [],
      subObjectId: 1,
    }
  },

  mounted() {
    this.getWorks()
    this.getSubObjects()
  },

  methods: {
    getWorks() {
      fetch(`http://localhost:8080/workings/${this.subObjectId}`,{
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.works = data
            console.log(data)
          })
    },
    deleteWork(id) {
      fetch(`http://localhost:8080/workings/${id}`, {
        method: 'DELETE'
      })
          .then(data => {
            console.log(data)
            this.getWorks()
          })
    },

    getSubObjects() {
      fetch(`http://localhost:8080/subobjects`,
      )
          .then(res => res.json())
          .then(data => {
            this.subObjects = data
          })
    },

    onChangeSubObject() {
      console.log(this.subObjectId)
      this.getWorks()
    },
  }
}

</script>