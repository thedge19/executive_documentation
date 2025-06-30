<template>
  <Navbar/>

  <div class="container py-5">
    <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
      <div class="card-header bg-white py-4">
        <h2 class="h4 mb-0 text-center text-primary">
          <i class="bi bi-pencil-square me-2"></i>
          Редактирование материала: {{ material.name }}
        </h2>
      </div>

      <div class="card-body">
        <form @submit.prevent="updateMaterial">
          <!-- Статус сертификата -->
          <div v-if="material.certificateUrl" class="alert alert-success mb-4">
            <i class="bi bi-check-circle-fill me-2"></i>
            Сертификат уже загружен
            <a :href="material.certificateUrl" target="_blank" class="ms-2">
              <i class="bi bi-download"></i> Скачать
            </a>
          </div>

          <!-- Загрузка файла -->
          <div v-if="!material.certificateUrl" class="mb-4">
            <label for="formFile" class="form-label fw-semibold">
              <i class="bi bi-file-earmark-pdf me-2"></i>
              Добавить сертификат (PDF)
            </label>
            <input @change="handleFileChange"
                   class="form-control"
                   type="file"
                   id="formFile"
                   accept=".pdf">
            <div class="form-text">Загрузите файл сертификата в формате PDF</div>
          </div>

          <!-- Информация о материале -->
          <div class="mb-4">
            <h5 class="fw-semibold mb-3">
              <i class="bi bi-info-circle me-2"></i>
              Информация о материале
            </h5>
            <div class="list-group">
              <div class="list-group-item">
                <strong>Единицы измерения:</strong> {{ material.units }}
              </div>
              <div class="list-group-item">
                <strong>Документы:</strong> {{ material.documents }}
              </div>
              <div class="list-group-item">
                <strong>Стандарт:</strong> {{ material.standard }}
              </div>
              <div class="list-group-item">
                <strong>Автор:</strong> {{ material.author }}
              </div>
              <div class="list-group-item">
                <strong>Страниц:</strong> {{ material.numberOfPages }}
              </div>
            </div>
          </div>

          <!-- Кнопки -->
          <div class="d-flex gap-3">
            <button type="button" @click="$router.push('/materials')"
                    class="btn btn-outline-secondary flex-grow-1 py-2">
              <i class="bi bi-arrow-left me-2"></i>Назад
            </button>

            <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                    :disabled="!selectedFile && !material.certificateUrl">
              <i class="bi bi-upload me-2"></i>
              {{ material.certificateUrl ? 'Заменить сертификат' : 'Загрузить сертификат' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
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
      error: null
    }
  },
  mounted() {
    this.getMaterial();
  },
  methods: {
    getMaterial() {
      fetch(`http://localhost:8080/materials/${this.$route.params.id}`)
          .then(res => {
            if (!res.ok) throw new Error('Материал не найден');
            return res.json();
          })
          .then(data => {
            this.material = data;
          })
          .catch(error => {
            console.error(error);
            this.error = error.message;
          });
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        if (file.type === 'application/pdf') {
          this.selectedFile = file;
          this.error = null;
        } else {
          this.error = 'Пожалуйста, загрузите файл в формате PDF';
          event.target.value = '';
        }
      }
    },
    async updateMaterial() {
      if (!this.selectedFile && !this.material.certificateUrl) {
        this.error = 'Выберите файл для загрузки';
        return;
      }

      try {
        const formData = new FormData();
        if (this.selectedFile) {
          formData.append("file", this.selectedFile);
        }

        const response = await fetch(`http://localhost:8080/materials/${this.$route.params.id}`, {
          method: 'PATCH',
          body: formData
        });

        if (!response.ok) {
          throw new Error('Ошибка при обновлении материала');
        }

        this.$router.push('/materials');
      } catch (error) {
        console.error(error);
        this.error = error.message;
      }
    }
  }
}
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.list-group-item {
  border-left: 0;
  border-right: 0;
  padding: 12px 16px;
}

.list-group-item:first-child {
  border-top: 0;
}

.list-group-item:last-child {
  border-bottom: 0;
}

.btn {
  border-radius: 8px;
  transition: all 0.2s;
}

.alert {
  border-radius: 8px;
}

.form-control {
  border-radius: 8px;
  padding: 10px 15px;
}

@media (max-width: 576px) {
  .card {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .container {
    padding-left: 0;
    padding-right: 0;
  }

  .d-flex {
    flex-direction: column;
    gap: 12px;
  }
}
</style>