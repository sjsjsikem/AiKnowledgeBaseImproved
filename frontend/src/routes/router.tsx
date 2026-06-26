import { Navigate, createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../layouts/AppLayout';
import { AuthLayout } from '../layouts/AuthLayout';
import { AdminPage } from '../pages/AdminPage';
import { DocumentEditorPage } from '../pages/DocumentEditorPage';
import { KnowledgeBasesPage } from '../pages/KnowledgeBasesPage';
import { LearningPage } from '../pages/LearningPage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ProtectedRoute } from './ProtectedRoute';

/**
 * router 是前端路由表。
 * 它使用 React Router 的 createBrowserRouter 组织认证页和受保护业务页，在本项目中定义用户从登录到业务模块的页面流转。
 */
export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/knowledge-bases" replace />,
  },
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: '/knowledge-bases', element: <KnowledgeBasesPage /> },
          { path: '/documents/:documentId', element: <DocumentEditorPage /> },
          { path: '/documents/new', element: <DocumentEditorPage /> },
          { path: '/admin', element: <AdminPage /> },
          { path: '/learning', element: <LearningPage /> },
        ],
      },
    ],
  },
]);
