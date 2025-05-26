import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import { setupCalendar, Calendar, DatePicker } from 'v-calendar';
import 'v-calendar/style.css';

const app = createApp(App)

app.use(router,
        setupCalendar, {}
)

app.mount('#app')
app.component('VCalendar', Calendar)
app.component('VDatePicker', DatePicker)