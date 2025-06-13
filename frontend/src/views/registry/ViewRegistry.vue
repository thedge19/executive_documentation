<template>
  <main>
    <Navbar/>
    <div class="container">
      <div class="row">
        <div style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
          <h1 class="text-center">Работы</h1>
          <div class="space-y-2 d-flex justify-content-start align-items-center">
            <div>
              <label class="input-group-text">Дата начала периода</label>
              <VDatePicker :attributes="attributes" v-model="startDate" mode="date"/>
            </div>
            <div class="m-lg-2">
              <label class="input-group-text">Дата окончания периода</label>
              <VDatePicker :attributes="attributes" v-model="endDate" :model-value="setFirstEndDate" mode="date"/>
            </div>
          </div>
          <div class="d-flex justify-content-start mt-2">
            <button @click.prevent="addDates" class="btn btn-outline-dark btn-block confirm-button m-lg-2">
              Сформировать реестр
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'Home',
  components: {
    Navbar
  },

  data() {
    return {
      startDate: new Date(),
      endDate: "",

      attributes: {
        highlight: true,
        dates: this.setFirstEndDate,
      }
    }
  },

  methods: {

    getSomething() {
      console.log(this.startDate);
      console.log(this.endDate);
    },

    async addDates() {
      try {
        const formatDate = (date) => {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        };

        const response = await fetch('http://localhost:8080/acts/registries', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            startDate: formatDate(this.startDate),
            endDate: formatDate(this.endDate)
          })
        });

        if (!response.ok) {
          throw new Error('Ошибка сервера');
        }

        // Получаем PDF как Blob
        const blob = await response.blob();

        // Создаем ссылку для скачивания
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        // Пытаемся получить имя файла из заголовков
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'реестр.pdf';

        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename\*?=([^;]+)/i);
          if (filenameMatch && filenameMatch[1]) {
            filename = decodeURIComponent(filenameMatch[1].replace(/UTF-8''/i, ''));
          }
        }

        a.download = filename;
        document.body.appendChild(a);
        a.click();

        // Очистка
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

      } catch (error) {
        console.error('Ошибка:', error);
        this.errors.push('Не удалось сформировать реестр');
      }
    },

    setFirstEndDate() {
      return this.startDate
    }
  },
}
</script>
