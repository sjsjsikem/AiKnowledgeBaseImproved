import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuthStore } from '../store/auth';

/**
 * LoginPage 是 Stage 1 的登录页面。
 * 它使用 auth.ts 的 login API、React Router 导航和 auth store，在本项目中完成前端登录表单到后端认证接口的闭环。
 */
export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  /**
   * handleSubmit 处理登录表单提交。
   * 它使用 React 自带的 FormEvent 阻止默认提交，调用 AuthController.java 的登录接口，并把 AuthResponse 写入 Zustand 登录态。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const response = await login({ username, password });
      setAuth(response);
      const from = (location.state as { from?: string } | null)?.from ?? '/knowledge-bases';
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '登录失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-card">
      <p className="eyebrow">Stage 1</p>
      <h2>登录</h2>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          用户名
          <input value={username} onChange={(event) => setUsername(event.target.value)} required />
        </label>
        <label>
          密码
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>{submitting ? '登录中' : '登录'}</button>
      </form>
      <Link to="/register">去注册</Link>
    </div>
  );
}
