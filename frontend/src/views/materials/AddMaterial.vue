<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить материал</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addMaterial">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-tag me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование материала"
                     required v-model="material.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-4">
              <label for="units" class="form-label fw-semibold">
                <i class="bi bi-rulers me-2"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control"
                     placeholder="Введите единицы измерения"
                     required v-model="material.units">
            </div>

            <!-- Паспорта сертификаты -->
            <div class="mb-4">
              <label class="form-label fw-semibold">
                <i class="bi bi-file-earmark-text me-2"></i>Документы
              </label>
              <div class="border rounded-3 p-3 bg-light-subtle">
                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <select class="form-select flex-grow-1"
                          v-model="documentType"
                          :class="{ 'is-invalid': documentsError && !documentType }"
                          style="min-width: 200px;">
                    <option value="" disabled selected>Выберите тип</option>
                    <option v-for="type in documentTypes" :value="type">{{ type }}</option>
                  </select>
                  <span class="text-nowrap">№</span>
                  <input type="text"
                         class="form-control flex-grow-1"
                         :class="{ 'is-invalid': documentsError && !documentNumber }"
                         placeholder="Номер"
                         v-model="documentNumber">
                </div>
                <div class="d-flex flex-wrap align-items-center gap-2">
                  <span class="text-nowrap">от</span>
                  <input type="text"
                         class="form-control flex-grow-1"
                         :class="{ 'is-invalid': documentsError && !documentDate }"
                         placeholder="Дата (например: 12.05.2023)"
                         v-model="documentDate">
                  <span class="text-nowrap">г.</span>
                </div>
                <div v-if="documentsError" class="invalid-feedback d-block">
                  <i class="bi bi-exclamation-circle me-1"></i>Заполните все поля документа
                </div>
              </div>
            </div>

            <!-- Загрузка файла -->
            <div class="mb-4">
              <label for="formFile" class="form-label fw-semibold">
                <i class="bi bi-file-earmark-pdf me-2"></i>Сертификат/паспорт (PDF)
              </label>
              <input @change="handleFileUpload" class="form-control"
                     type="file" id="formFile" accept=".pdf">
              <small class="text-muted" v-if="selectedFile">Выбран файл: {{ selectedFile.name }}</small>

              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-2">
                <div class="progress" style="height: 24px;">
                  <div class="progress-bar progress-bar-striped progress-bar-animated"
                       :style="{ width: uploadProgress + '%' }">
                    {{ uploadProgress }}%
                  </div>
                </div>
              </div>

              <div v-if="uploadError" class="alert alert-danger mt-2">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ uploadError }}
              </div>
            </div>

            <!-- Автор сертификата -->
            <div class="mb-4">
              <label for="author" class="form-label fw-semibold">
                <i class="bi bi-person me-2"></i>Автор сертификата
              </label>
              <input id="author" type="text" class="form-control"
                     placeholder="Введите автора сертификата"
                     required v-model="material.author">
            </div>

            <!-- Число страниц -->
            <div class="mb-4">
              <label for="numberOfPages" class="form-label fw-semibold">
                <i class="bi bi-file-text me-2"></i>Число страниц
              </label>
              <input id="numberOfPages" type="number" class="form-control"
                     placeholder="Введите число страниц"
                     required v-model="material.numberOfPages">
            </div>

            <!-- ГОСТ, ТУ -->
            <div class="mb-4">
              <label for="standard" class="form-label fw-semibold">
                <i class="bi bi-file-earmark-check me-2"></i>ГОСТ, ТУ
              </label>
              <input id="standard" type="text" class="form-control"
                     placeholder="Введите ГОСТ или ТУ"
                     required v-model="material.standard">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2" :disabled="isUploading">
                <template v-if="isUploading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Загрузка...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Сохранить материал
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import {computed, ref} from 'vue';
import { useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';

const router = useRouter();

const documentType = ref('Сертификат соответствия');
const documentNumber = ref('');
const documentDate = ref('');
const documentTypes = ref([
  'Сертификат соответствия',
  'Сертификат качества',
  'Паспорт изделия',
  'Письмо'
]);

const formattedDocuments = computed(() => {
  return `${documentType.value} №${documentNumber.value} от ${documentDate.value} г.`;
});

const material = ref({
  name: '',
  units: '',
  documents: '',
  author: '',
  standard: '',
  numberOfPages: ''
});

const selectedFile = ref(null);
const uploadProgress = ref(0);
const uploadError = ref(null);
const documentsError = ref(false);
const isUploading = ref(false);
const error = ref(null);
const fileRequired = ref(true);

const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (!file) {
    selectedFile.value = null;
    return;
  }

  // Валидация файла
  if (file.type !== 'application/pdf') {
    uploadError.value = 'Пожалуйста, загрузите файл в формате PDF';
    selectedFile.value = null;
    return;
  }

  selectedFile.value = file;
  uploadError.value = null;
};

const validateDocuments = () => {
  const isValid = documentType.value && documentNumber.value && documentDate.value;
  documentsError.value = !isValid;
  return isValid;
};

const validateForm = () => {
  let isValid = true;

  if (fileRequired.value && !selectedFile.value) {
    uploadError.value = 'Пожалуйста, загрузите файл сертификата';
    isValid = false;
  }

  if (!validateDocuments()) {
    isValid = false;
  }

  return isValid;
};

const addMaterial = async () => {
  if (!validateForm()) return;

  try {
    isUploading.value = true;
    error.value = null;
    uploadError.value = null;

    material.value.documents = formattedDocuments.value;

    const formData = new FormData();
    formData.append('material', new Blob([JSON.stringify(material.value)], {
      type: 'application/json'
    }));

    if (selectedFile.value) {
      formData.append('file', selectedFile.value);
    }

    const response = await fetch('http://localhost:8080/materials', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Ошибка при сохранении материала');
    }

    router.push("/materials");
  } catch (err) {
    error.value = err.message || 'Произошла ошибка';
    console.error('Error:', err);
  } finally {
    isUploading.value = false;
    uploadProgress.value = 0;
  }
};
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control, .form-select {
  border-radius: 8px;
  padding: 10px 15px;
}

.form-label {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.btn {
  border-radius: 8px;
  transition: all 0.2s;
}

.alert {
  border-radius: 8px;
}

.progress {
  border-radius: 8px;
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
}
</style>