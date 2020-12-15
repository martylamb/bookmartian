const BundleAnalyzerPlugin = require('webpack-bundle-analyzer')
    .BundleAnalyzerPlugin;

module.exports = {
  outputDir: '../main/resources/static-content',
  configureWebpack: {
    // plugins: [new BundleAnalyzerPlugin()]
  },
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:4567',
        secure: false,
        pathRewrite: {
          // '^/api': '/v2/api',
        },
      }
    },
  },
  pluginOptions: {
    fontawesome: {
    
      // Defines the names of all the Font Awesome
      // components that will be imported (optional)
      components: {
        icon:       'fa-icon',
        layers:     'fa-layers',
        layersText: 'fa-layers-text',
      },
    
      // Lists the imported icons
      imports: [
        {
          set: '@fortawesome/free-solid-svg-icons',
          icons: [
            'search',
            'AngleRight',
            'EllipsisH',
            'Plus'
          ]
        }
      ]
    }    
  }
}