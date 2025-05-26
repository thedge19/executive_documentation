<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Update SubObject</h2>
        <form @submit.prevent="updateSubObject">
          <!--name-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="name" class="form-label">Name</label>
              <input id="name" type="text" name="name" class="form-control" placeholder="Name" required
                     v-model="material.name">
            </div>
          </div>


          <!--Email-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="email" class="form-label">Email</label>
              <input id="email" type="email" name="email" class="form-control" placeholder="email"
                     required v-model="material.email">
            </div>
          </div>

          <!--Phone Number-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="pNo" class="form-label">Phone Number</label>
              <input id="pNo" type="text" name="pNo" class="form-control" placeholder="Phone Number"
                     required v-model="material.pNo">
            </div>
          </div>


          <div class="row">
            <div class="col-md-12 form-group">
              <input class="btn btn-primary w-100" type="submit" value="Submit">
            </div>
          </div>

          <div class="row">
            <button @click.prevent="getSomething" class="btn btn-outline-success w-50" type="submit" value="Submit">
              Жми
            </button>
          </div>

          <div>

          </div>
        </form>

      </div>
    </div>

  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'UpdateSubObject',
  components: {
    Navbar
  },

  data() {
    return {
      material: {
        id: '',
        name: '',
        units: '',
        documents: '',
        standard: ''
      }
    }
  },

  beforeMount() {
    this.getSubObjects();
  },

  methods: {
    getSubObjects() {
      fetch(`http://localhost:8080/subobjects/${this.$route.params.id}`)
          .then(res => res.json())
          .then(data => {
            this.material = data;
            console.log(this.material);
          })

    },
    updateSubObject() {
      fetch(`http://localhost:8080/subobjects`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.material)
      })
          .then(data => {
            console.log(data);
            this.$router.push('/subObjects');
          })
    },
    getSomething() {
      console.log(this.$route.params.id)
    },
  }
}

</script>