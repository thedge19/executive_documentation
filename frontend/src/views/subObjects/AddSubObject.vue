<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить подобъект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addSubObject">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-building me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование подобъекта"
                     required v-model="subObject.name">
            </div>

            <!-- Аббревиатура -->
            <div class="mb-4">
              <label for="title" class="form-label fw-semibold">
                <i class="bi bi-textarea-t me-2"></i>Аббревиатура
              </label>
              <input id="title" type="text" class="form-control"
                     placeholder="Введите аббревиатуру"
                     required v-model="subObject.title">
            </div>

            <!-- Выбор проекта -->
            <div class="mb-4">
              <label class="form-label fw-semibold d-block mb-3">
                <i class="bi bi-diagram-2 me-2"></i>Проект
              </label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" name="projectId"
                       id="project1" autocomplete="off"
                       :value="1" v-model="subObject.projectId">
                <label class="btn btn-outline-primary" for="project1">
                  <i class="bi bi-tree me-2"></i>Грушовая
                </label>

                <input type="radio" class="btn-check" name="projectId"
                       id="project2" autocomplete="off"
                       :value="2" v-model="subObject.projectId">
                <label class="btn btn-outline-primary" for="project2">
                  <i class="bi bi-building me-2"></i>Шесхарис
                </label>
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-3">
              <button @click.prevent="getSomething"
                      class="btn btn-outline-success flex-grow-1 py-2">
                <i class="bi bi-lightning-charge me-2"></i>Проверить
              </button>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2">
                <i class="bi bi-check-circle me-2"></i>Добавить
              </button>
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
  name: 'AddSubObject',
  components: {
    Navbar
  },
  data() {
    return {
      subObject: {
        name: '',
        title: '',
        projectId: 1, // Установлено значение по умолчанию 1
      },
      error: null
    }
  },
  methods: {
    getSomething() {
      console.log('Выбран проект ID:', this.subObject.projectId);
    },

    addSubObject() {
      this.error = null;

      fetch('http://localhost:8080/subobjects', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.subObject)
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при добавлении подобъекта');
            }
            return response.json();
          })
          .then(data => {
            console.log(data);
            this.$router.push("/subObjects");
          })
          .catch(error => {
            console.error('Ошибка:', error);
            this.error = error.message;
          });
    }
  }
}
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control {
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

.btn-group {
  gap: 8px;
}

.btn-group .btn {
  flex: 1;
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