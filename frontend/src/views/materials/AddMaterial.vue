<template>
  <main :style="{'background-image': 'url(/09-12-2016_yuzhno-russkoe_2.jpg)', 'min-height': '100vh'}">
    <Navbar/>

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
            <div class="mt-2">
              <label class="form-label fw-semibold">
                <i class="bi bi-file-earmark-text me-2"></i>Документы
              </label>

              <!-- Выбор количества документов -->
              <div class="mb-3">
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" id="docCount1"
                         v-model="documentsCount" value="1" checked>
                  <label class="form-check-label" for="docCount1">1 документ</label>
                </div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" id="docCount2"
                         v-model="documentsCount" value="2">
                  <label class="form-check-label" for="docCount2">2 документа</label>
                </div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" id="docCount3"
                         v-model="documentsCount" value="3">
                  <label class="form-check-label" for="docCount3">3 документа</label>
                </div>
              </div>

              <!-- Контейнер для форм документов -->
              <div v-for="(doc, index) in documents" :key="index"
                   class="border rounded-3 p-3 bg-light-subtle mb-3">
                <h6 class="mb-3">Документ №{{ index + 1 }}</h6>

                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <select class="form-select flex-grow-1"
                          v-model="doc.type"
                          :class="{ 'is-invalid': documentsError && !doc.type }"
                          style="min-width: 200px;">
                    <option value="" disabled selected>Выберите тип</option>
                    <option v-for="type in documentTypes" :value="type">{{ type }}</option>
                  </select>
                  <span class="text-nowrap">№</span>
                  <input type="text"
                         class="form-control flex-grow-1"
                         :class="{ 'is-invalid': documentsError && !doc.number }"
                         placeholder="Номер"
                         v-model="doc.number">
                </div>

                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <span class="text-nowrap">от</span>
                  <input type="date"
                         class="form-control flex-grow-1"
                         :class="{ 'is-invalid': documentsError && !doc.date }"
                         placeholder="Дата (например: 12.05.2023)"
                         v-model="doc.date">
                  <span class="text-nowrap">г.</span>
                </div>

                <div v-if="documentsError && !isDocumentValid(doc)" class="invalid-feedback d-block">
                  <i class="bi bi-exclamation-circle me-1"></i>Заполните все поля документа
                </div>

                <!-- Загрузка файла -->
                <div class="mt-2">
                  <label :for="'formFile'+index" class="form-label fw-semibold">
                    <i class="bi bi-file-earmark-pdf me-2"></i>Сертификат/паспорт (PDF)
                  </label>
                  <input @change="(e) => handleFileUpload(e, index)" class="form-control"
                         type="file" :id="'formFile'+index" accept=".pdf">
                  <small class="text-muted" v-if="doc.file">Выбран файл: {{ doc.file.name }}</small>

                  <div v-if="doc.uploadProgress > 0 && doc.uploadProgress < 100" class="mt-2">
                    <div class="progress" style="height: 24px;">
                      <div class="progress-bar progress-bar-striped progress-bar-animated"
                           :style="{ width: doc.uploadProgress + '%' }">
                        {{ doc.uploadProgress }}%
                      </div>
                    </div>
                  </div>

                  <div v-if="doc.uploadError" class="alert alert-danger mt-2">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ doc.uploadError }}
                  </div>
                </div>
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
import {ref, watch} from 'vue';
import {useRouter} from 'vue-router';
import Navbar from '../../components/Navbar.vue';

const router = useRouter();

const documentsCount = ref(1);
const documents = ref([
  {
    type: 'Сертификат соответствия',
    number: '',
    date: '',
    file: null,
    uploadProgress: 0,
    uploadError: null
  }
]);

const documentTypes = ref([
  'Сертификат соответствия',
  'Сертификат качества',
  'Паспорт изделия',
  'Письмо'
]);

// Следим за изменением количества документов
watch(documentsCount, (newVal) => {
  const count = parseInt(newVal);
  if (count > documents.value.length) {
    while (documents.value.length < count) {
      documents.value.push({
        type: 'Сертификат соответствия',
        number: '',
        date: '',
        numberOfPages: null, // Добавлено новое поле
        file: null,
        uploadProgress: 0,
        uploadError: null
      });
    }
  } else {
    documents.value = documents.value.slice(0, Math.max(1, count));
  }
});

const material = ref({
  name: '',
  units: '',
  documents: '',
  author: '',
  standard: '',
});

const isUploading = ref(false);
const documentsError = ref(false);
const error = ref(null);
const fileRequired = ref(true);

const isDocumentValid = (doc) => {
  return doc.type && doc.number && doc.date && doc.numberOfPages && (!fileRequired.value || doc.file);
};

const handleFileUpload = (event, index) => {
  const file = event.target.files[0];
  if (!file) {
    documents.value[index].file = null;
    return;
  }

  // Валидация файла
  if (file.type !== 'application/pdf') {
    documents.value[index].uploadError = 'Пожалуйста, загрузите файл в формате PDF';
    documents.value[index].file = null;
    return;
  }

  documents.value[index].file = file;
  documents.value[index].uploadError = null;
};

const validateDocuments = () => {
  const allValid = documents.value.every(isDocumentValid);
  documentsError.value = !allValid;
  return allValid;
};

const validateForm = () => {
  let isValid = true;

  if (!validateDocuments()) {
    isValid = false;
  }

  return isValid;
};

const formatDateForDisplay = (isoDate) => {
  if (!isoDate) return '';
  const [year, month, day] = isoDate.split('-');
  return `${day}.${month}.${year}`;
};

const addMaterial = async () => {
  if (!validateForm()) return;

  try {
    isUploading.value = true;
    error.value = null;

    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Токен отсутствует';
      return;
    }

    const formData = new FormData();

    // Добавляем данные материала
    formData.append('material', new Blob([JSON.stringify({
      name: material.value.name,
      units: material.value.units,
      standard: material.value.standard,
      certificates: documents.value.map(doc => ({
        name: `${doc.type} №${doc.number} от ${formatDateForDisplay(doc.date)} г.`,
        author: material.value.author,
        type: doc.type,
        number: doc.number,
        date: doc.date,  // ISO format (YYYY-MM-DD)
        dateFormatted: formatDateForDisplay(doc.date) // Дополнительно, если нужно
      }))
    })], { type: 'application/json' }));

    // Добавляем файлы
    documents.value.forEach((doc, index) => {
      if (doc.file) {
        formData.append(`files`, doc.file);
      }
    });

    const response = await fetch('http://localhost:8080/materials', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      error.value = errorData.message;
      return;
    }

    await router.push("/materials");
  } catch (err) {
    error.value = err.message || 'Произошла ошибка';
    console.error('Error:', err);
  } finally {
    isUploading.value = false;
    documents.value.forEach(doc => doc.uploadProgress = 0);
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