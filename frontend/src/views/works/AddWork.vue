<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Добавить работы</h2>
        <form @submit.prevent="addWork">
          <!--name-->
          <div class="input-group mb-3 mt-3">
            <label class="input-group-text" for="inputGroupSelect01">
              {{ subObject.name }}
            </label>
          </div>

          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="name" class="form-label">Наименование</label>
              <input id="name" type="text" name="name" class="form-control" placeholder="наименование"
                     required v-model="work.name">
            </div>
          </div>


          <!--Units-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="units" class="form-label">Ед. изм.</label>
              <input id="units" type="text" name="units" class="form-control" placeholder="ед. изм."
                     required v-model="work.units">
            </div>
          </div>

          <!--Quantity-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="pNo" class="form-label">Количество</label>
              <input id="quantity" type="number" step="0.001" name="quantity" class="form-control"
                     placeholder="Количество" required v-model="work.quantity">
            </div>
          </div>

          <!--Standard-->
          <div class="input-group mb-3 mt-3">
            <label class="input-group-text" for="inputGroupSelect01">СП:</label>
            <select class="form-select" id="inputGroupSelect01"
                    v-model="work.standardId" required>
              <option value="" selected disabled>Выберите стандарт...</option>
              <option style="width: min-content"
                      v-for="standard in standards" :value="standard.id">{{ standard.name }}
              </option>
            </select>
          </div>

          <div v-if="error" class="alert alert-danger">{{ error }}</div>

          <div class="row">
            <div class="col-md-12 form-group">
              <input class="btn btn-primary w-25" type="submit" value="Submit">
            </div>
          </div>
<!--          <div class="row">-->
<!--            <button @click.prevent="getSomething" class="btn btn-outline-success w-50" type="submit" value="Submit">-->
<!--              Жми-->
<!--            </button>-->
<!--          </div>-->
        </form>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'AddWork',
  components: {
    Navbar
  },

  data() {
    return {
      projectId: 1,
      error: '',
      subObject: "",
      standards: [],

      work: {
        name: '',
        units: '',
        quantity: '',
        done: 0,
        standardId: '',
        subObjectId: this.$route.params.id
      }
    }
  },

  mounted() {
    this.getSubObject(this.$route.params.id);
    this.getStandards();
  },

  methods: {

    getSomething() {
      console.log(this.work)
    },

    addWork() {
      // Проверка выбранного стандарта
      if (!this.work.standardId) {
        this.error = 'Пожалуйста, выберите стандарт';
        return;
      }

      this.error = ''; // Очищаем сообщение об ошибке, если оно было
      fetch('http://localhost:8080/workings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },


        body: JSON.stringify(this.work)
      })
          .then(data => {
            console.log(data)
            this.$router.push(`/works/${this.$route.params.id}`);
          })
    },

    getSubObject() {
      fetch(`http://localhost:8080/subobjects/subObject/${this.$route.params.id}`,
      )
          .then(res => res.json())
          .then(data => {
            this.subObject = data
          })
    },

    getStandards() {
      fetch(`http://localhost:8080/standards`,
      )
          .then(res => res.json())
          .then(data => {
            this.standards = data
          })
    },
  },

}


</script>