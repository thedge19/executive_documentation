<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Добавить подобъект</h2>
        <form @submit.prevent="addSubObject">
          <!--name-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="name" class="form-label">Наименование</label>
              <input id="name" type="text" name="name" class="form-control" placeholder="наименование"
                     required v-model="subObject.name">
            </div>
          </div>


          <!--Email-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="units" class="form-label">Аббревиатура</label>
              <input id="units" type="text" name="units" class="form-control" placeholder="Аббревиатура"
                     required v-model="subObject.title">
            </div>
          </div>

          <!--Phone Number-->
          <div class="row">
            <div class="d-flex justify-content-start mt-3">
              <label class="radio mr-1">
                <input type="radio" name="projectId" :value="1"
                       v-model="subObject.projectId"
                       checked>
                <span> <i class="fa fa-user"></i> Грушовая </span>
              </label>
              <label class="radio m-lg-auto">
                <input type="radio" name="projectId" :value="2"
                       v-model="subObject.projectId">
                <span> <i class="fa fa-plus-circle"></i> Шесхарис </span>
              </label>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12 form-group">
              <input class="btn btn-primary w-100" type="submit" value="Submit">
            </div>
          </div>
          <div class="row mt-3">
            <div class="col-md-12 form-group">
              <button @click.prevent="getSomething" class="btn btn-outline-success w-50" type="submit" value="Submit">Жми</button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'AddSubObject',
  components: {
    Navbar
  },

  data() {
    return {
      subObject: {
        name: '',
        title: '',
        projectId: 4,
      }
    }
  },

  methods: {

    getSomething() {
      console.log(this.subObject.projectId);
    },

    addSubObject() {
      fetch('http://localhost:8080/subobjects', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.subObject)
      })
          .then(data => {
            console.log(data)
            this.$router.push("/subObjects");
          })
    }
  }
}


</script>