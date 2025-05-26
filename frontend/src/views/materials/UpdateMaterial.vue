<template>
    <main>
        <Navbar/>
      <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
        <div class="my-5 py-lg-5 mx-auto w-50">
          <div class="mx-auto py-lg-5">
            <h2 class="text-center mb-3">Редактирование {{ material.name }}</h2>
            <form @submit.prevent="updateMaterial">
              <h2 v-if="material.certificateId != null">Сертификат добавлен</h2>
              <div v-if="material.certificateId == null" class="mb-3 ">
                <label for="formFile" class="form-label">Добавьте сертификат/паспорт</label>
                <input  @change="addCertificate" class="form-control border border-primary" type="file" id="formFile">
              </div>
            </form>
          </div>
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
                certificate_id: null
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

        addCertificate(event) {
          this.selectedFile = event.target.files[0];
          const formData = new FormData();
          console.log(this.selectedFile);
          formData.append("file", this.selectedFile);
          fetch(`http://localhost:8080/materials/certificate/${this.$route.params.id}`, {
            method: 'POST',
            body: formData
          })
              .then(data => {
                console.log(data);
                this.$router.push('/materials');
              })
        }
    }
}

</script>