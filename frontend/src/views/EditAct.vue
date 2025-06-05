<script setup>
import Navbar from "@/components/Navbar.vue";
</script>
<template>
  <Navbar/>
  <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
    <div class="my-5 py-lg-5 mx-auto w-50">
      <div class="mx-auto py-lg-5">
        <h2 class="text-center mb-3">Редактирование акта № {{ act.actNumber }}</h2>
        <h2 class="text-center mb-3">{{ act.works }}</h2>
        <form @submit.prevent="updateAct">

<!--          <div class="row">-->
<!--            <div class="col-md-12 form-group mb-3 py-lg-5">-->
<!--              <label for="name" class="form-label">Работы</label>-->
<!--              <textarea class="form-control h-auto" id="name" type="text" name="name"-->
<!--                        required v-model="act.works">-->
<!--            </textarea>-->
<!--            </div>-->
<!--          </div>-->
          <h2 v-if="act.executiveSchemaId != null">Исполнительная схема добавлена.</h2>
          <div v-if="act.executiveSchemaId == null" class="mb-3 ">
            <label for="formFile" class="form-label">Добавьте схему</label>
            <input  @change="updateAct" class="form-control border border-primary" type="file" id="formFile">
          </div>
          <div class="row">
            <div class="col-md-12 form-group" style="width: 50%">
              <input class="btn btn-primary w-30" type="submit" value="Submit">
            </div>
          </div>

        </form>
      </div>
    </div>
  </div>
</template>
<script>

export default {
  name: 'EditAct',

  data() {
    return {
      act: {
        id: '',
        actNumber: null,
        works: '',
        executiveSchemaId: null,
      },
      selectedFile: null,
    }
  },

  mounted() {
    this.getAct();
  },

  methods: {
    getAct() {
      fetch(`http://localhost:8080/acts/${this.$route.params.id}`,)
          .then(res => res.json())
          .then(data => {
            this.act = data
            console.log(data)
          })
    },

    updateAct(event) {
      this.selectedFile = event.target.files[0];
      const formData = new FormData();
      console.log(this.selectedFile);
      formData.append("file", this.selectedFile);
      fetch(`http://localhost:8080/acts/${this.$route.params.id}`, {
        method: 'PATCH',
        // headers: {
        //   'Accept': 'application/json',
        //   'Content-Type': 'multipart/form-data'
        // },
        body: formData
      })
          .then(data => {
            console.log(data);
            this.$router.push('/');
          })
    },
  }
}

</script>

<style scoped>
body {
  background-color: #FFEBEE
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
</style>