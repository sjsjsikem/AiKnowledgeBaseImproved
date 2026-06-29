import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router-dom';
import { router } from './routes/router';
import './styles.css';

/**
 * queryClient 是 TanStack Query 的全局客户端。
 * 它配置查询默认行为，在本项目中统一管理前端服务端状态缓存和请求重试策略。
 */
const queryClient = new QueryClient({
  // defaultOptions 是 TanStack Query 自带全局配置，用于统一控制查询行为。
  defaultOptions: {
    // queries 配置所有 useQuery 的默认行为，在本项目中避免窗口聚焦时频繁刷新教学接口。
    queries: {
      refetchOnWindowFocus: false,
    },
  },
});

/**
 * ReactDOM.createRoot 是 React 18 自带的应用挂载入口。
 * 它把 QueryClientProvider 和 RouterProvider 注入到页面根节点，在本项目中启动整个前端单页应用。
 */
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </React.StrictMode>,
);
