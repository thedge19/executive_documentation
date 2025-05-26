<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Редактирование работ</h2>
        <form @submit.prevent="updateWork">

          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="name" class="form-label">Наименование</label>
              <input id="name" type="text" name="name" class="form-control"
                     required v-model="work.name">
            </div>
          </div>
          <!--Units-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="units" class="form-label">Ед. изм.</label>
              <input id="units" type="text" name="units" class="form-control"
                     required v-model="work.units">
            </div>
          </div>
          <!--Quantity-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="quantity" class="form-label">Количество</label>
              <input id="quantity" type="number" step="0.01" name="quantity" class="form-control"
                     required v-model="work.quantity">
            </div>
          </div>
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="done" class="form-label">Выполнено</label>
              <input id="done" type="number" step="0.01" name="done" class="form-control"
                     required v-model="work.done">
            </div>
          </div>
          <div class="row">
            <div class="col-md-12 form-group" style="width: 50%">
              <input class="btn btn-primary w-30" type="submit" value="Submit">
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
  name: 'UpdateWork',
  components: {
    Navbar
  },

  data() {
    return {
      work: {
        id: '',
        name: '',
        units: '',
        quantity: '',
        done: ''
      }
    }
  },

  mounted() {
    this.getWork();
  },

  methods: {
    getWork() {
      fetch(`http://localhost:8080/workings/working/${this.$route.params.id}`,)
          .then(res => res.json())
          .then(data => {
            this.work = data
            console.log(data)
          })
    },

    updateWork() {
      fetch(`http://localhost:8080/workings/${this.$route.params.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.work)
      })
          .then(data => {
            console.log(data);
            this.$router.push('/works');
          })
    },
  }
}

</script>