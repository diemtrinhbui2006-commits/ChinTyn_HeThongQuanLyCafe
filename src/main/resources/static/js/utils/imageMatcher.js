/**
 * Tiện ích tự động khớp hình ảnh sản phẩm từ tên món
 */

export function getProductImage(name) {
  const n = (name || '').toLowerCase();
  if (n.includes('bạc xỉu') || n.includes('bac xiu') || n.includes('bạc sỉu')) return '/uploads/bac_xiu.jpg';
  if (n.includes('muối') || n.includes('muoi')) return '/uploads/caphe_muoi.jpg';
  if (n.includes('sữa đá') || n.includes('sua da') || n.includes('nâu đá')) return '/uploads/caphe_suada.jpg';
  if (n.includes('đen') || n.includes('den')) return '/uploads/caphe_denda.jpg';
  if (n.includes('cốt dừa') || n.includes('dừa')) return '/uploads/caphe_cotdua.jpg';
  if (n.includes('cappuccino') || n.includes('latte')) return '/uploads/cappuccino.jpg';
  if (n.includes('đào') || n.includes('dao')) return '/uploads/tradao_camsa.jpg';
  if (n.includes('vải') || n.includes('vai')) return '/uploads/tra_vai.jpg';
  if (n.includes('matcha')) return '/uploads/trasua_matcha.jpg';
  if (n.includes('ô long') || n.includes('olong')) return '/uploads/trasua_olong.jpg';
  if (n.includes('trân châu') || n.includes('trà sữa')) return '/uploads/trasua_tranchau.jpg';
  if (n.includes('cam')) return '/uploads/nuoc_cam.jpg';
  if (n.includes('bơ') || n.includes('sinh tố')) return '/uploads/sinhto_bo.jpg';
  if (n.includes('tiramisu')) return '/uploads/tiramisu.jpg';
  if (n.includes('cheesecake') || n.includes('cheese')) return '/uploads/cheesecake.jpg';
  if (n.includes('croissant') || n.includes('bánh')) return '/uploads/croissant.jpg';
  return null;
}
