<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Редактирование материалов: {{ material.name }}</h2>
        <form @submit.prevent="updateMaterial">
          <h2 v-if="material.certificateUrl != null">Сертификат добавлен.</h2>
          <div v-if="material.certificateUrl == null" class="mb-3 ">
            <label for="formFile" class="form-label">Добавьте сертификат</label>
            <input @change="updateMaterial" class="form-control border border-primary" type="file" id="formFile">
          </div>
          <div class="row">
            <div class="col-md-12 form-group" style="width: 50%">
              <input class="btn btn-primary w-30" type="submit" value="Обновить">
            </div>
          </div>

          <!--          </form> -->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group mb-3">-->
          <!--                <label for="name" class="form-label">Наименование</label>-->
          <!--                <input id="name" type="text" name="name" class="form-control"-->
          <!--                       required v-model="material.name">-->
          <!--              </div>-->
          <!--            </div>-->
          <!--            &lt;!&ndash;Units&ndash;&gt;-->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group mb-3">-->
          <!--                <label for="units" class="form-label">Ед. изм.</label>-->
          <!--                <input id="units" type="text" name="units" class="form-control"-->
          <!--                       required v-model="material.units">-->
          <!--              </div>-->
          <!--            </div>-->
          <!--            &lt;!&ndash;Quantity&ndash;&gt;-->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group mb-3">-->
          <!--                <label for="quantity" class="form-label">Документ о качестве</label>-->
          <!--                <input id="quantity" type="text" name="quantity" class="form-control"-->
          <!--                       required v-model="material.documents">-->
          <!--              </div>-->
          <!--            </div>-->
          <!--            &lt;!&ndash;Standard&ndash;&gt;-->
          <!--            <div class="input-group mb-3 mt-3">-->
          <!--              <label class="input-group-text" for="inputGroupSelect01">Автор</label>-->
          <!--              <input id="author" type="text" name="author" class="form-control"-->
          <!--                      v-model="material.author">-->
          <!--            </div>-->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group mb-3">-->
          <!--                <label for="numberOfPages" class="form-label">Число страниц в сертификате (паспорте)</label>-->
          <!--                <input id="numberOfPages" type="number" name="numberOfPages" class="form-control"-->
          <!--                       placeholder="0" required v-model="material.numberOfPages">-->
          <!--              </div>-->
          <!--            </div>-->
          <!--            &lt;!&ndash;ГОСТ, ТУ&ndash;&gt;-->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group mb-3">-->
          <!--                <label for="documents" class="form-label">ГОСТ, ТУ</label>-->
          <!--                <input id="documents" type="text" name="documents" class="form-control"-->
          <!--                       placeholder="ГОСТ, ТУ" required v-model="material.standard">-->
          <!--              </div>-->
          <!--            </div>-->
          <!--            <div class="row">-->
          <!--              <div class="col-md-12 form-group" style="width: 50%">-->
          <!--                <input class="btn btn-primary w-30" type="submit" value="Обновить">-->
          <!--              </div>-->
          <!--            </div>-->
        </form>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'UpdateMaterial',
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
        standard: '',
        author: '',
        numberOfPages: '',
        certificateUrl: '',
      },
      selectedFile: null,
    }
  },

  mounted() {
    this.getMaterial();
  },

  methods: {
    getMaterial() {
      fetch(`http://localhost:8080/materials/${this.$route.params.id}`)
          .then(res => res.json())
          .then(data => {
            this.material = data;
            console.log(this.material);
          })
    },

    updateMaterial(event) {
      this.selectedFile = event.target.files[0];
      const formData = new FormData();
      console.log(this.selectedFile);
      formData.append("file", this.selectedFile);
      fetch(`http://localhost:8080/materials/${this.$route.params.id}`, {
        method: 'PATCH',
        // headers: {
        //   'Content-Type': 'application/json'
        // },
        body: formData
      })
          .then(data => {
            console.log(data);
            this.$router.push(`/materials`);
          })
    },
  }
}

</script>