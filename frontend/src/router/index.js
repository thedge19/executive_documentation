import {createRouter, createWebHistory} from 'vue-router'
import Home from "@/views/Home.vue";
import ViewMaterials from "@/views/materials/ViewMaterials.vue";
import ViewProjects from "@/views/projects/ViewProjects.vue";
import ViewStandards from "@/views/standards/ViewStandards.vue";
import ViewSubObjects from "@/views/subObjects/ViewSubObjects.vue";
import ViewWorks from "@/views/works/ViewWorks.vue";
import ViewControl from "@/views/controls/ViewControl.vue";
import ViewSchemas from "@/views/schemas/ViewSchemas.vue";
import Login from "@/views/Login.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/addAct',
      name: 'addAct',
      component: () => import('../views/AddAct.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editAct/:id',
      name: 'editAct',
      component: () => import('../views/EditAct.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects',
      name: 'viewProjects',
      component: ViewProjects,
      meta: { requiresAuth: true }
    },
    {
      path: '/addProject',
      name: 'addProject',
      component: () => import('../views/projects/AddProject.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editProject/:id',
      name: 'editProject',
      component: () => import('../views/projects/UpdateProject.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/materials',
      name: 'viewMaterials',
      component: ViewMaterials,
      meta: { requiresAuth: true }
    },
    {
      path: '/addMaterial',
      name: 'addMaterial',
      component: () => import('../views/materials/AddMaterial.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editMaterial/:id',
      name: 'editMaterial',
      component: () => import('../views/materials/UpdateMaterial.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/standards',
      name: 'viewStandards',
      component: ViewStandards,
      meta: { requiresAuth: true }
    },
    {
      path: '/addStandard',
      name: 'addStandard',
      component: () => import('../views/standards/AddStandard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editStandard/:id',
      name: 'editStandard',
      component: () => import('../views/standards/UpdateStandard.vue')
    },
    {
      path: '/subObjects/:id',
      name: 'viewSubObjects',
      component: ViewSubObjects,
      meta: { requiresAuth: true }
    },
    {
      path: '/addSubObject',
      name: 'addSubObject',
      component: () => import('../views/subObjects/AddSubObject.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editSubObject/:id',
      name: 'editSubObject',
      component: () => import('../views/subObjects/UpdateSubObject.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/works/:id',
      name: 'viewWorks',
      component: ViewWorks,
      meta: { requiresAuth: true }
    },
    {
      path: '/addWork/:id',
      name: 'addWork',
      component: () => import('../views/works/AddWork.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editWork/:id',
      name: 'editWork',
      component: () => import('../views/works/UpdateWork.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/controls',
      name: 'viewControls',
      component: ViewControl,
      meta: { requiresAuth: true }
    },
    {
      path: '/editControl/:id',
      name: 'editControl',
      component: () => import('../views/controls/UpdateControl.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/workLog3',
      name: 'viewWorkLog3',
      component: () => import('../views/workLog/WorkLog.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/workLog6',
      name: 'viewWorkLog6',
      component: () => import('../views/workLog/WorkLog6.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/schemas',
      name: 'viewSchemas',
      component: ViewSchemas,
      meta: { requiresAuth: true }
    },
  ],
})

// Глобальный навигационный хук
router.beforeEach(async (to, from, next) => {
  // Маршруты, не требующие авторизации
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // Проверяем наличие токена
  const token = localStorage.getItem('token')

  // Если токена нет - перенаправляем на логин
  if (!token) {
    next('/login')
    return
  }

  try {
    // Проверяем валидность токена на сервере
    const response = await fetch('http://localhost:8080/api/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.ok) {
      next() // Токен валиден, разрешаем переход
    } else {
      // Токен невалиден, очищаем и перенаправляем
      localStorage.removeItem('token')
      next('/login')
    }
  } catch (error) {
    console.error('Ошибка проверки авторизации:', error)
    localStorage.removeItem('token')
    next('/login')
  }
})

export default router
