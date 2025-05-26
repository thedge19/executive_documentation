<script setup>
import Navbar from "@/components/Navbar.vue";
</script>
<template>
  <main>
    <Navbar/>
    <div class="mx-auto mt-5 w-25" style="max-width:100%;">
      <div class="px-1 py-4">
        <h6 class="card-title mb-3">Определить период выборки?</h6>
        <div class="form-check">
          <input class="form-check-input" type="radio" v-model="isPeriodSelected" :value=false name="flexRadioDefault" id="flexRadioDefault1" checked>
          <label class="form-check-label" for="flexRadioDefault1">
            Нет
          </label>
        </div>
        <div class="form-check">
          <input class="form-check-input" type="radio" v-model="isPeriodSelected" :value=true name="flexRadioDefault" id="flexRadioDefault2">
          <label class="form-check-label" for="flexRadioDefault2">
            Да
          </label>
        </div>
        <div v-if="isPeriodSelected">
          <div class="space-y-2 d-flex justify-content-between">
            <div>
              <label class="input-group-text">Начало выборки</label>
              <VDatePicker :attributes="attributes" v-model="startDate" mode="date"/>
            </div>
            <div>
              <label class="input-group-text">Конец выборки</label>
              <VDatePicker :attributes="attributes" v-model="endDate" mode="date"/>
            </div>
          </div>
        </div>
        <div class="d-flex justify-content-start mt-3">
          <button type="button" class="btn btn-outline-success mx-3" @click="writePDF">Выгрузить в pdf</button>
        </div>
      </div>
    </div>
  </main>
</template>
<script>

export default {
  name: "SelectPeriod",

  data() {
    return {
      isPeriodSelected: false,

      startDate: new Date("2025.03.01"),
      endDate: new Date(),

      attributes: {
        highlight: true,
        dates: new Date(),
      }
    }
  },

  methods: {
    writePDF() {
      fetch('http://localhost:8080/acts/toPDF',{
        method: 'POST',
            headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          startDate: this.startDate.toDateString(),
          endDate: this.endDate.toDateString(),
        })
      })
    .then(data => {
        console.log(data)
        this.$router.push("/");
      })
    },

    deleteSheets() {
      fetch('http://localhost:8080/acts/excel', {
        method: 'DELETE'
      })
    },

    showSomething() {
      console.log(new Date("2024.09.02"))
    }
  },
}
</script>
<style scoped>
body {
  background-color: #FFEBEE
}

.card {
  width: 400px;
  background-color: #fff;
  border: none;
  border-radius: 12px
}

label.radio {
  cursor: pointer;
  width: 100%
}

label.radio input {
  position: absolute;
  top: 0;
  left: 0;
  visibility: hidden;
  pointer-events: none
}

label.radio span {
  padding: 7px 14px;
  border: 2px solid #eee;
  display: inline-block;
  color: #039be5;
  border-radius: 10px;
  width: 100%;
  height: 48px;
  line-height: 27px
}

label.radio input:checked + span {
  border-color: #039BE5;
  background-color: #81D4FA;
  color: #fff;
  border-radius: 9px;
  height: 48px;
  line-height: 27px
}

.form-control {
  margin-top: 10px;
  height: 48px;
  border: 2px solid #eee;
  border-radius: 10px
}

.form-control:focus {
  box-shadow: none;
  border: 2px solid #039BE5
}

.agree-text {
  font-size: 12px
}

.terms {
  font-size: 12px;
  text-decoration: none;
  color: #039BE5
}

.confirm-button {
  height: 50px;
  border-radius: 10px
}
</style>