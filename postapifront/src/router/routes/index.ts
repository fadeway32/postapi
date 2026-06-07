import type { ElegantConstRoute } from '@elegant-router/types';
import { generatedRoutes } from '../elegant/routes';
import { layouts, views } from '../elegant/imports';
import { transformElegantRoutesToVueRoutes } from '../elegant/transform';

/**
 * custom routes
 *
 * @link https://github.com/soybeanjs/elegant-router?tab=readme-ov-file#custom-route
 */
const customRoutes: ElegantConstRoute[] = [
  {
    name: 'postadmin',
    path: '/postadmin',
    component: 'layout.base',
    meta: {
      title: 'PostAdmin',
      i18nKey: 'route.postadmin',
      icon: 'carbon:api',
      order: 1
    },
    children: [
      {
        name: 'postadmin_dashboard',
        path: '/postadmin/dashboard',
        component: 'view.postadmin_dashboard',
        meta: {
          title: 'Dashboard',
          i18nKey: 'route.postadmin_dashboard',
          icon: 'mdi:monitor-dashboard',
          order: 1,
          fixedIndexInTab: 1
        }
      },
      {
        name: 'postadmin_groups',
        path: '/postadmin/groups',
        component: 'view.postadmin_groups',
        meta: {
          title: 'API Groups',
          i18nKey: 'route.postadmin_groups',
          icon: 'carbon:folder',
          order: 2
        }
      },
      {
        name: 'postadmin_definitions',
        path: '/postadmin/definitions',
        component: 'view.postadmin_definitions',
        meta: {
          title: 'API Definitions',
          i18nKey: 'route.postadmin_definitions',
          icon: 'carbon:api-1',
          order: 3
        }
      },
      {
        name: 'postadmin_runtime',
        path: '/postadmin/runtime',
        component: 'view.postadmin_runtime',
        meta: {
          title: 'Runtime',
          i18nKey: 'route.postadmin_runtime',
          icon: 'carbon:play',
          order: 4
        }
      },
      {
        name: 'postadmin_groovy',
        path: '/postadmin/groovy',
        component: 'view.postadmin_groovy',
        meta: {
          title: 'Groovy Engine',
          i18nKey: 'route.postadmin_groovy',
          icon: 'carbon:code',
          order: 5
        }
      },
      {
        name: 'postadmin_logs',
        path: '/postadmin/logs',
        component: 'view.postadmin_logs',
        meta: {
          title: 'Logs',
          i18nKey: 'route.postadmin_logs',
          icon: 'carbon:document',
          order: 6
        }
      },
      {
        name: 'postadmin_tenants',
        path: '/postadmin/tenants',
        component: 'view.postadmin_tenants',
        meta: {
          title: 'Tenants',
          i18nKey: 'route.postadmin_tenants',
          icon: 'carbon:enterprise',
          order: 7
        }
      },
      {
        name: 'postadmin_tenant',
        path: '/postadmin/tenant',
        redirect: '/postadmin/tenants',
        meta: {
          title: 'Tenants',
          i18nKey: 'route.postadmin_tenants',
          hideInMenu: true
        }
      }
    ]
  },
  {
    name: 'exception',
    path: '/exception',
    component: 'layout.base',
    meta: {
      title: 'exception',
      i18nKey: 'route.exception',
      icon: 'ant-design:exception-outlined',
      order: 7
    },
    children: [
      {
        name: 'exception_403',
        path: '/exception/403',
        component: 'view.403',
        meta: {
          title: 'exception_403',
          i18nKey: 'route.exception_403',
          icon: 'ic:baseline-block'
        }
      },
      {
        name: 'exception_404',
        path: '/exception/404',
        component: 'view.404',
        meta: {
          title: 'exception_404',
          i18nKey: 'route.exception_404',
          icon: 'ic:baseline-web-asset-off'
        }
      },
      {
        name: 'exception_500',
        path: '/exception/500',
        component: 'view.500',
        meta: {
          title: 'exception_500',
          i18nKey: 'route.exception_500',
          icon: 'ic:baseline-wifi-off'
        }
      }
    ]
  },
  {
    name: 'document',
    path: '/document',
    component: 'layout.base',
    meta: {
      title: 'document',
      i18nKey: 'route.document',
      order: 2,
      icon: 'mdi:file-document-multiple-outline'
    },
    children: [
      {
        name: 'document_antd',
        path: '/document/antd',
        component: 'view.iframe-page',
        props: {
          url: 'https://antdv.com/components/overview-cn'
        },
        meta: {
          title: 'document_antd',
          i18nKey: 'route.document_antd',
          order: 7,
          icon: 'logos:ant-design'
        }
      },
      {
        name: 'document_naive',
        path: '/document/naive',
        component: 'view.iframe-page',
        props: {
          url: 'https://www.UI.com/zh-CN/os-theme/docs/introduction'
        },
        meta: {
          title: 'document_naive',
          i18nKey: 'route.document_naive',
          order: 6,
          icon: 'logos:naiveui'
        }
      },
      {
        name: 'document_element-plus',
        path: '/document/element-plus',
        component: 'view.iframe-page',
        props: {
          url: 'https://element-plus.org/zh-CN/'
        },
        meta: {
          title: 'document_element-plus',
          i18nKey: 'route.document_element-plus',
          order: 7,
          icon: 'ep:element-plus'
        }
      },
      {
        name: 'document_alova',
        path: '/document/alova',
        component: 'view.iframe-page',
        props: {
          url: 'https://alova.js.org'
        },
        meta: {
          title: 'document_alova',
          i18nKey: 'route.document_alova',
          order: 8,
          localIcon: 'alova'
        }
      },
      {
        name: 'document_project',
        path: '/document/project',
        component: 'view.iframe-page',
        props: {
          url: 'https://docs.soybeanjs.cn/zh'
        },
        meta: {
          title: 'document_project',
          i18nKey: 'route.document_project',
          order: 1,
          localIcon: 'logo'
        }
      },
      {
        name: 'document_project-link',
        path: '/document/project-link',
        component: 'view.iframe-page',
        meta: {
          title: 'document_project-link',
          i18nKey: 'route.document_project-link',
          order: 2,
          localIcon: 'logo',
          // use href to open the page, the routeName must be ends with '-link'
          href: 'https://docs.soybeanjs.cn/zh'
        }
      },
      {
        name: 'document_unocss',
        path: '/document/unocss',
        component: 'view.iframe-page',
        props: {
          url: 'https://unocss.dev/'
        },
        meta: {
          title: 'document_unocss',
          i18nKey: 'route.document_unocss',
          order: 5,
          icon: 'logos:unocss'
        }
      },
      {
        name: 'document_vite',
        path: '/document/vite',
        component: 'view.iframe-page',
        props: {
          url: 'https://cn.vitejs.dev/'
        },
        meta: {
          title: 'document_vite',
          i18nKey: 'route.document_vite',
          order: 4,
          icon: 'logos:vitejs'
        }
      },
      {
        name: 'document_vue',
        path: '/document/vue',
        component: 'view.iframe-page',
        props: {
          url: 'https://cn.vuejs.org/'
        },
        meta: {
          title: 'document_vue',
          i18nKey: 'route.document_vue',
          order: 3,
          icon: 'logos:vue'
        }
      }
    ]
  }
];

/** create routes when the auth route mode is static */
export function createStaticRoutes() {
  const constantRoutes: ElegantConstRoute[] = [];

  const authRoutes: ElegantConstRoute[] = [];

  const generatedAppRoutes = generatedRoutes.filter(item => item.name !== 'postadmin');

  [...customRoutes, ...generatedAppRoutes].forEach(item => {
    if (item.meta?.constant) {
      constantRoutes.push(item);
    } else {
      authRoutes.push(item);
    }
  });

  return {
    constantRoutes,
    authRoutes
  };
}

/**
 * Get auth vue routes
 *
 * @param routes Elegant routes
 */
export function getAuthVueRoutes(routes: ElegantConstRoute[]) {
  return transformElegantRoutesToVueRoutes(routes, layouts, views);
}
