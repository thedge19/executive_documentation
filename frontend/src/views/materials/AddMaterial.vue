<template>
  <main>
    <Navbar/>
    <div class="my-5">
      <div class="mx-auto w-25 " style="max-width:100%;">
        <h2 class="text-center mb-3">Добавить материал</h2>
        <form @submit.prevent="addMaterial">
          <!--name-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="name" class="form-label">Наименование</label>
              <input id="name" type="text" name="name" class="form-control" placeholder="наименование"
                     required v-model="material.name">
            </div>
          </div>


          <!--Email-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="units" class="form-label">Ед. изм.</label>
              <input id="units" type="text" name="units" class="form-control" placeholder="ед. изм."
                     required v-model="material.units">
            </div>
          </div>

          <!--Phone Number-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="pNo" class="form-label">Паспорта сертификаты</label>
              <input id="documents" type="text" name="documents" class="form-control"
                     placeholder="Паспорта сертификаты" required v-model="material.documents">
            </div>
          </div>
          <div class="mb-3">
            <label for="formFile" class="form-label">Добавьте сертификат/паспорт (PDF)</label>
            <input @change="handleFileUpload" class="form-control border border-primary"
                   type="file" id="formFile" accept=".pdf">
            <small class="text-muted" v-if="selectedFile">Выбран файл: {{ selectedFile.name }}</small>
            <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-2">
              <div class="progress">
                <div class="progress-bar" :style="{ width: uploadProgress + '%' }">
                  {{ uploadProgress }}%
                </div>
              </div>
            </div>
            <div v-if="uploadError" class="alert alert-danger mt-2">
              {{ uploadError }}
            </div>
          </div>
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="pNo" class="form-label">Автор сертификата</label>
              <input id="author" type="text" name="author" class="form-control"
                     placeholder="Автор сертификата" required v-model="material.author">
            </div>
          </div>
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="numberOfPages" class="form-label">Число страниц в сертификате (паспорте)</label>
              <input id="numberOfPages" type="number" name="numberOfPages" class="form-control"
                     placeholder="0" required v-model="material.numberOfPages">
            </div>
          </div>
          <!--ГОСТ, ТУ-->
          <div class="row">
            <div class="col-md-12 form-group mb-3">
              <label for="pNo" class="form-label">ГОСТ, ТУ</label>
              <input id="documents" type="text" name="documents" class="form-control"
                     placeholder="ГОСТ, ТУ" required v-model="material.standard">
            </div>
          </div>

          <div class="row">
            <div class="col-md-12 form-group">
              <input class="btn btn-primary w-100" type="submit" value="Submit">
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
  name: 'AddMaterial',
  components: {
    Navbar
  },
  data() {
    return {
      material: {
        name: '',
        units: '',
        documents: '',
        author: '',
        standard: '',
        numberOfPages: ''
      },
      selectedFile: null,
      uploadProgress: 0,
      uploadError: null,
      isUploading: false,
      fileRequired: true
    }
  },

  methods: {
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (!file) {
        this.selectedFile = null;
        return;
      }

      // Валидация файла
      if (file.type !== 'application/pdf') {
        this.uploadError = 'Пожалуйста, загрузите файл в формате PDF';
        this.selectedFile = null;
        return;
      }

      this.selectedFile = file;
      this.uploadError = null;
    },

    validateForm() {
      if (this.fileRequired && !this.selectedFile) {
        this.uploadError = 'Пожалуйста, загрузите файл сертификата';
        return false;
      }
      return true;
    },

    async addMaterial() {
      // Проверка перед отправкой
      if (!this.validateForm()) {
        return;
      }

      try {
        this.isUploading = true;
        this.uploadError = null;

        const formData = new FormData();

        // Добавляем данные материала как JSON
        formData.append('material', new Blob([JSON.stringify(this.material)], {
          type: 'application/json'
        }));

        // Добавляем файл, если он есть
        if (this.selectedFile) {
          formData.append('file', this.selectedFile);
        }

        const response = await fetch('http://localhost:8080/materials', {
          method: 'POST',
          body: formData,
          onUploadProgress: (progressEvent) => {
            if (progressEvent.lengthComputable) {
              this.uploadProgress = Math.round(
                  (progressEvent.loaded * 100) / progressEvent.total
              );
            }
          }
        });

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Ошибка при сохранении материала');
        }

        this.$router.push("/materials");
      } catch (error) {
        this.uploadError = error.message || 'Произошла ошибка';
        console.error('Error:', error);
      } finally {
        this.isUploading = false;
        this.uploadProgress = 0;
      }
    }
  }
}
</script>