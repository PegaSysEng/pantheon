function numberWithCommas(x) {
  if (x) {
    const parts = x.toString().split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  }
  return x;
}
export default {
  methods: {
    numberWithCommas,
  },
};
