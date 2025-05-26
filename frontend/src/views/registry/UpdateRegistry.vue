<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h3 class="text-center mb-3">Редактирование количества страниц реестра</h3>
        <form @submit.prevent="updateRegistry">
          <!--name-->
          <div class="row mt-3" style="width:30%;">
            <div class="col-md-12 form-group">
              <input v-model="numberOfSheets" type="number" name="numberOfSheets">
            </div>
          </div>

          <button class="btn btn-primary w-50 mt-3" type="submit">Обновить</button>
        </form>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'UpdateRegistry',
  components: {
    Navbar
  },

  data() {
    return {
      numberOfSheets: 1,
    }
  },

  methods: {
    updateRegistry() {
      fetch(`http://localhost:8080/registries/${this.$route.params.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: this.numberOfSheets
      })
          .then(data => {
            console.log(data);
            this.$router.push('/registry');
          })
    }
  }
}

</script>