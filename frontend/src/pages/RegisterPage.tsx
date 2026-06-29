import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuthStore } from '../store/auth';

/**
 * RegisterPage 是 Stage 1 的注册页面。
 * 它使用 auth.ts 的 register API、React Router 导航和 auth store，在本项目中完成新用户创建并自动进入登录态。
 */
export function RegisterPage() {
  // navigate 来自 React Router，用于注册成功后跳转到知识库入口页。
  const navigate = useNavigate();
  // setAuth 来自 auth.ts 的 Zustand store，用于保存注册成功后返回的登录态。
  const setAuth = useAuthStore((state) => state.setAuth);
  // username 使用 React 自带 useState 保存注册表单中的用户名。
  const [username, setUsername] = useState('');
  // nickname 使用 React 自带 useState 保存注册表单中的昵称。
  const [nickname, setNickname] = useState('');
  // email 使用 React 自带 useState 保存注册表单中的邮箱，可为空。
  const [email, setEmail] = useState('');
  // password 使用 React 自带 useState 保存注册表单中的密码。
  const [password, setPassword] = useState('');
  // error 使用 React 自带 useState 保存注册失败消息，并展示在表单中。
  const [error, setError] = useState('');
  // submitting 使用 React 自带 useState 标记注册请求是否正在提交，用于禁用按钮和显示 loading 文案。
  const [submitting, setSubmitting] = useState(false);

  /**
   * handleSubmit 处理注册表单提交。
   * 它使用 React 自带的 FormEvent 阻止浏览器默认刷新，调用 AuthController.java 的注册接口，并保存返回的 AuthResponse。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      // response 来自 auth.ts 的 register API，包含后端 AuthResponse.java 返回的 Token 和用户资料。
      const response = await register({ username, nickname, email: email || undefined, password });
      setAuth(response);
      navigate('/knowledge-bases', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '注册失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-card">
      <p className="eyebrow">Stage 1</p>
      <h2>注册</h2>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          用户名
          <input value={username} onChange={(event) => setUsername(event.target.value)} minLength={3} required />
        </label>
        <label>
          昵称
          <input value={nickname} onChange={(event) => setNickname(event.target.value)} required />
        </label>
        <label>
          邮箱
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" placeholder="user@example.com" />
        </label>
        <label>
          密码
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" minLength={6} required />
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>{submitting ? '注册中' : '注册'}</button>
      </form>
      <Link to="/login">已有账号，去登录</Link>
    </div>
  );
}
