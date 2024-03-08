import { createApp } from 'vue'
import App from './App.vue'
import 'vant/es/toast/style'
import {Button, Icon, NavBar, Tabbar, TabbarItem} from "vant";

// 将 vue 的页面文件和 dom 元素关联起来, mount 是挂载
const app = createApp(App);

app.use(Button);
app.use(NavBar);
app.use(Icon);
app.use(Tabbar);
app.use(TabbarItem);

app.mount('#app')