/** @type {import('tailwindcss').Config} */

module.exports = {
    darkMode: 'class',
    content: ["../resources/templates/**/*.{html,js}", "node_modules/preline/dist/*.js"],
    // content: [],
    theme: {
        extend: {},
    },
    plugins: [
        // forms,
        // accordion,
    ],
}