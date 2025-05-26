<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h3 class="text-center mb-3">Редактирование акта входного контроля № {{ control.controlNumber }}</h3>
        <h3 class="text-center mb-3">{{ control.materials }}</h3>
        <form @submit.prevent="updateControl">
          <!--name-->
          <div class="row mt-3" style="width:30%;">
            <div class="col-md-12 form-group">
              <input v-model="author">
            </div>
          </div>
          <div class="row mt-3" style="width:30%;">
            <div class="col-md-12 form-group">
              <input v-model="controlSheetNumbers" type="number">
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
  name: 'UpdateControl',
  components: {
    Navbar
  },

  data() {
    return {
      control: {},

      authors: ['000 «Керама Марацци»',
        'Soudal NV', 'АО "АРИКОН"', 'ВОЛМА-ВОСКРЕСЕНСК', 'ЗАО "ВМЗ"',
        'ЗАО "ЭНЕРГОКОМПЛЕКТ"',
        'Качественные смеси', 'МЗ БАЛАКОВО', 'НISENER INDUSTRIAL',
        'ООО "ВОЛМА-Байкал"', 'ООО "Грида"', 'ООО "ДЗ1"', 'ООО "ДКС"',
        'ООО "КАЗ"', 'ООО "Калита"', 'ООО "ОСНОВА"', 'ООО "ПК "СТАНК""',
        'ООО "РТЗ"', 'ООО "УГЛИЧКАБЕЛЬ"', 'ООО «БКУ»', 'ООО «ЗЛКС»',
        'ООО «ЗЛКС»', 'ООО НПО "СПЕКТР"', 'ПАО "НМЗ"', 'ПАО "НМЗ"', 'УГМК СТАЛЬ'],

      id: '',
      author: '',
      controlSheetNumbers: null,
    }
  },

  mounted() {
    this.getControl();
  },

  methods: {
    getControl() {
      fetch(`http://localhost:8080/acts/entrance/${this.$route.params.id}`)
          .then(res => res.json())
          .then(data => {
            this.control = data;
            console.log(this.control);
          })

    },
    updateControl() {
      fetch(`http://localhost:8080/acts/entrance/${this.$route.params.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          id: this.$route.params.id,
          author: this.author,
          controlSheetNumbers: this.controlSheetNumbers,
        })
      })
          .then(data => {
            console.log(data);
            this.$router.push('/controls');
          })
    }
  }
}

</script>