import store from '@/store'
import defaultSettings from '@/settings'

/**
 * 动态修改标题
 */
export function useDynamicTitle() {
  const systemTitle = defaultSettings.title || '农产品销售平台'
  const pageTitle = store.state.settings.title || systemTitle
  if (store.state.settings.dynamicTitle) {
    document.title = pageTitle === systemTitle ? systemTitle : pageTitle + ' - ' + systemTitle
  } else {
    document.title = systemTitle
  }
}
